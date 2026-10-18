package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.epam.gymcrm.service.Validation.requireText;

/**
 * Trainer use cases. Except for {@link #create}, every method first authenticates the caller and then
 * works on that caller's own profile. Each public method runs in one transaction.
 */
@Service
@Transactional
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;
    private final AuthenticationService authenticationService;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;

    public TrainerService(TrainerDao trainerDao,
                          TrainingTypeDao trainingTypeDao,
                          AuthenticationService authenticationService,
                          UsernameGenerator usernameGenerator,
                          PasswordGenerator passwordGenerator) {
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
        this.authenticationService = authenticationService;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
    }

    public Trainer create(String firstName, String lastName, String specializationName) {
        String first = requireText(firstName, "First name");
        String last = requireText(lastName, "Last name");
        TrainingType specialization = findTrainingType(specializationName);
        log.info("Creating trainer profile for {} {}", first, last);

        User user = new User(first, last, usernameGenerator.generate(first, last), passwordGenerator.generate());
        Trainer created = trainerDao.save(new Trainer(user, specialization));
        log.info("Created trainer profile with username '{}'", user.getUsername());
        return created;
    }

    @Transactional(readOnly = true)
    public boolean credentialsMatch(Credentials credentials) {
        log.debug("Checking trainer credentials for '{}'", credentials.username());
        return authenticationService.matchesTrainer(credentials);
    }

    @Transactional(readOnly = true)
    public Trainer getProfile(Credentials credentials) {
        Trainer trainer = authenticationService.authenticateTrainer(credentials);
        log.debug("Selected trainer profile '{}'", credentials.username());
        return trainer;
    }

    public void changePassword(Credentials credentials, String newPassword) {
        Trainer trainer = authenticationService.authenticateTrainer(credentials);
        trainer.getUser().setPassword(requireText(newPassword, "New password"));
        log.info("Changed password of trainer '{}'", credentials.username());
    }

    public Trainer update(Credentials credentials, String firstName, String lastName, String specializationName) {
        Trainer trainer = authenticationService.authenticateTrainer(credentials);
        trainer.getUser().setFirstName(requireText(firstName, "First name"));
        trainer.getUser().setLastName(requireText(lastName, "Last name"));
        trainer.setSpecialization(findTrainingType(specializationName));
        log.info("Updated trainer profile '{}'", credentials.username());
        return trainerDao.update(trainer);
    }

    /** Not idempotent: an active trainer becomes inactive and the other way round. Returns the new state. */
    public boolean toggleActivation(Credentials credentials) {
        User user = authenticationService.authenticateTrainer(credentials).getUser();
        user.toggleActive();
        log.info("Trainer '{}' is now {}", credentials.username(), user.isActive() ? "active" : "inactive");
        return user.isActive();
    }

    private TrainingType findTrainingType(String name) {
        String typeName = requireText(name, "Specialization");
        return trainingTypeDao.findByName(typeName).orElseThrow(() -> {
            log.warn("Unknown training type '{}'", typeName);
            return new EntityNotFoundException("Training type", typeName);
        });
    }
}
