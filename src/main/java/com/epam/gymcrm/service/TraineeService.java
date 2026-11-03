package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.request.RegisterTraineeRequest;
import com.epam.gymcrm.dto.request.UpdateTraineeRequest;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.GymCrmMapper;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Trainee use cases. Except for {@link #create}, every method first authenticates the caller and then
 * works on that caller's own profile. Each public method runs in one transaction. Requests are validated
 * at the REST boundary, so the service does not repeat that.
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
    private final GymCrmMapper mapper;

    public TraineeService(
            TraineeDao traineeDao,
            TrainerDao trainerDao,
            AuthenticationService authenticationService,
            UsernameGenerator usernameGenerator,
            PasswordGenerator passwordGenerator,
            GymCrmMapper mapper
    ) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.authenticationService = authenticationService;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
        this.mapper = mapper;
    }

    public Trainee create(RegisterTraineeRequest request) {
        Credentials generated = new Credentials(
                usernameGenerator.generate(request.firstName(), request.lastName()),
                passwordGenerator.generate()
        );
        Trainee created = traineeDao.save(mapper.toTrainee(request, generated));
        log.info("Created trainee profile with username '{}'", generated.username());
        return created;
    }

    @Transactional(readOnly = true)
    public Trainee getProfile(Credentials credentials) {
        Trainee trainee = authenticationService.authenticateTrainee(credentials);
        log.debug("Selected trainee profile '{}'", credentials.username());
        return trainee;
    }

    public Trainee update(Credentials credentials, UpdateTraineeRequest request) {
        Trainee trainee = authenticationService.authenticateTrainee(credentials);
        trainee.getUser().setFirstName(request.firstName().trim());
        trainee.getUser().setLastName(request.lastName().trim());
        trainee.getUser().setActive(request.isActive());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        log.info("Updated trainee profile '{}'", credentials.username());
        return traineeDao.update(trainee);
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

        requested.removeAll(trainers.stream()
                .map(trainer -> trainer.getUser().getUsername())
                .toList());
        if (!requested.isEmpty()) {
            log.warn("Cannot update trainers of '{}': unknown trainers {}", credentials.username(), requested);
            throw new EntityNotFoundException("Trainer", String.join(", ", requested));
        }

        trainee.replaceTrainers(trainers);
        log.info("Trainee '{}' now has {} assigned trainer(s)", credentials.username(), trainers.size());
        return traineeDao.update(trainee);
    }
}
