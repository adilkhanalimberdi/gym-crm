package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.TrainerProfile;
import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.UpdateTrainerRequest;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.epam.gymcrm.service.Validation.requireNonNull;
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
    private final TraineeDao traineeDao;
    private final TrainingTypeDao trainingTypeDao;
    private final AuthenticationService authenticationService;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final TrainerMapper mapper;

    public TrainerService(TrainerDao trainerDao,
                          TraineeDao traineeDao,
                          TrainingTypeDao trainingTypeDao,
                          AuthenticationService authenticationService,
                          UsernameGenerator usernameGenerator,
                          PasswordGenerator passwordGenerator,
                          TrainerMapper mapper) {
        this.trainerDao = trainerDao;
        this.traineeDao = traineeDao;
        this.trainingTypeDao = trainingTypeDao;
        this.authenticationService = authenticationService;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
        this.mapper = mapper;
    }

    public Trainer create(CreateTrainerRequest request) {
        String first = requireText(request.firstName(), "First name");
        String last = requireText(request.lastName(), "Last name");
        TrainingType specialization = findTrainingType(request.specializationId());
        log.info("Creating trainer profile for {} {}", first, last);

        Credentials generated = new Credentials(usernameGenerator.generate(first, last), passwordGenerator.generate());
        Trainer created = trainerDao.save(mapper.toEntity(request, specialization, generated));
        log.info("Created trainer profile with username '{}'", generated.username());
        return created;
    }

    @Transactional(readOnly = true)
    public TrainerProfile getProfile(Credentials credentials) {
        Trainer trainer = authenticationService.authenticateTrainer(credentials);
        log.debug("Selected trainer profile '{}'", credentials.username());
        return profileOf(trainer);
    }

    public TrainerProfile update(Credentials credentials, UpdateTrainerRequest request) {
        Trainer trainer = authenticationService.authenticateTrainer(credentials);
        trainer.getUser().setFirstName(requireText(request.firstName(), "First name"));
        trainer.getUser().setLastName(requireText(request.lastName(), "Last name"));
        trainer.getUser().setActive(request.active());
        log.info("Updated trainer profile '{}'", credentials.username());
        return profileOf(trainerDao.update(trainer));
    }

    private TrainerProfile profileOf(Trainer trainer) {
        return new TrainerProfile(trainer, traineeDao.findAllByTrainer(trainer.getUser().getUsername()));
    }

    private TrainingType findTrainingType(Long id) {
        Long typeId = requireNonNull(id, "Specialization");
        return trainingTypeDao.findById(typeId).orElseThrow(() -> {
            log.warn("Unknown training type id {}", typeId);
            return new EntityNotFoundException("Training type", String.valueOf(typeId));
        });
    }
}
