package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.UpdateTraineeRequest;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TraineeMapper;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.epam.gymcrm.service.Validation.requireText;

/**
 * Trainee use cases. Except for {@link #create}, every method first authenticates the caller and then
 * works on that caller's own profile. Each public method runs in one transaction.
 */
@Service
@Transactional
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final AuthenticationService authenticationService;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final TraineeMapper mapper;

    public TraineeService(TraineeDao traineeDao,
                          TrainerDao trainerDao,
                          AuthenticationService authenticationService,
                          UsernameGenerator usernameGenerator,
                          PasswordGenerator passwordGenerator,
                          TraineeMapper mapper) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.authenticationService = authenticationService;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
        this.mapper = mapper;
    }

    public Trainee create(CreateTraineeRequest request) {
        String first = requireText(request.firstName(), "First name");
        String last = requireText(request.lastName(), "Last name");
        log.info("Creating trainee profile for {} {}", first, last);

        Credentials generated = new Credentials(usernameGenerator.generate(first, last), passwordGenerator.generate());
        Trainee created = traineeDao.save(mapper.toEntity(request, generated));
        log.info("Created trainee profile with username '{}'", generated.username());
        return created;
    }

    @Transactional(readOnly = true)
    public boolean credentialsMatch(Credentials credentials) {
        log.debug("Checking trainee credentials for '{}'", credentials.username());
        return authenticationService.matchesTrainee(credentials);
    }

    @Transactional(readOnly = true)
    public Trainee getProfile(Credentials credentials) {
        Trainee trainee = authenticationService.authenticateTrainee(credentials);
        log.debug("Selected trainee profile '{}'", credentials.username());
        return trainee;
    }

    public void changePassword(Credentials credentials, String newPassword) {
        Trainee trainee = authenticationService.authenticateTrainee(credentials);
        trainee.getUser().setPassword(requireText(newPassword, "New password"));
        log.info("Changed password of trainee '{}'", credentials.username());
    }

    public Trainee update(Credentials credentials, UpdateTraineeRequest request) {
        Trainee trainee = authenticationService.authenticateTrainee(credentials);
        trainee.getUser().setFirstName(requireText(request.firstName(), "First name"));
        trainee.getUser().setLastName(requireText(request.lastName(), "Last name"));
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        log.info("Updated trainee profile '{}'", credentials.username());
        return traineeDao.update(trainee);
    }

    /** Not idempotent: an active trainee becomes inactive and the other way round. Returns the new state. */
    public boolean toggleActivation(Credentials credentials) {
        User user = authenticationService.authenticateTrainee(credentials).getUser();
        user.toggleActive();
        log.info("Trainee '{}' is now {}", credentials.username(), user.isActive() ? "active" : "inactive");
        return user.isActive();
    }

    /** Hard delete; the user record and all trainings of the trainee are removed with it. */
    public void delete(Credentials credentials) {
        Trainee trainee = authenticationService.authenticateTrainee(credentials);
        traineeDao.delete(trainee);
        log.info("Deleted trainee profile '{}' together with its trainings", credentials.username());
    }

    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(Credentials credentials) {
        authenticationService.authenticateTrainee(credentials);
        List<Trainer> trainers = trainerDao.findNotAssignedToTrainee(credentials.username());
        log.debug("Found {} trainers not assigned to trainee '{}'", trainers.size(), credentials.username());
        return trainers;
    }

    /** Replaces the trainee's trainers with the trainers that have the given usernames. */
    public Trainee updateTrainers(Credentials credentials, Collection<String> trainerUsernames) {
        Trainee trainee = authenticationService.authenticateTrainee(credentials);
        Set<String> requested = new HashSet<>(trainerUsernames);
        List<Trainer> trainers = trainerDao.findAllByUsernames(requested);

        requested.removeAll(trainers.stream().map(trainer -> trainer.getUser().getUsername()).toList());
        if (!requested.isEmpty()) {
            log.warn("Cannot update trainers of '{}': unknown trainers {}", credentials.username(), requested);
            throw new EntityNotFoundException("Trainer", String.join(", ", requested));
        }

        trainee.replaceTrainers(trainers);
        log.info("Trainee '{}' now has {} assigned trainer(s)", credentials.username(), trainers.size());
        return traineeDao.update(trainee);
    }
}
