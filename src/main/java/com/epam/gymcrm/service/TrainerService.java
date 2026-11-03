package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.TrainerProfile;
import com.epam.gymcrm.dto.request.RegisterTrainerRequest;
import com.epam.gymcrm.dto.request.UpdateTrainerRequest;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.GymCrmMapper;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Trainer use cases. Except for {@link #create}, every method first authenticates the caller and then
 * works on that caller's own profile. Each public method runs in one transaction. Requests are validated
 * at the REST boundary, so the service does not repeat that.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TrainerService {

    private final TrainerDao trainerDao;
    private final TraineeDao traineeDao;
    private final TrainingTypeDao trainingTypeDao;
    private final AuthenticationService authenticationService;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final GymCrmMapper mapper;

    public Trainer create(RegisterTrainerRequest request) {
        TrainingType specialization = findTrainingType(request.specializationId());
        Credentials generated = new Credentials(
                usernameGenerator.generate(request.firstName(), request.lastName()),
                passwordGenerator.generate()
        );
        Trainer created = trainerDao.save(mapper.toTrainer(
                request,
                specialization,
                generated
        ));
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
        trainer.getUser().setFirstName(request.firstName().trim());
        trainer.getUser().setLastName(request.lastName().trim());
        trainer.getUser().setActive(request.isActive());
        log.info("Updated trainer profile '{}'", credentials.username());
        return profileOf(trainerDao.update(trainer));
    }

    private TrainerProfile profileOf(Trainer trainer) {
        return new TrainerProfile(trainer, traineeDao.findAllByTrainer(trainer.getUser().getUsername()));
    }

    private TrainingType findTrainingType(Long typeId) {
        return trainingTypeDao.findById(typeId).orElseThrow(() -> {
            log.warn("Unknown training type id {}", typeId);
            return new EntityNotFoundException("Training type", String.valueOf(typeId));
        });
    }
}
