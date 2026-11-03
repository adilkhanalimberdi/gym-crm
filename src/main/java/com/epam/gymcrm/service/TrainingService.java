package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.TraineeTrainingCriteria;
import com.epam.gymcrm.dto.TrainerTrainingCriteria;
import com.epam.gymcrm.dto.request.AddTrainingRequest;
import com.epam.gymcrm.exception.AccessDeniedException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidRequestException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Training use cases; every method first authenticates the caller and runs in one transaction. */
@Service
@Transactional
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private final TrainingDao trainingDao;
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final AuthenticationService authenticationService;

    public TrainingService(
            TrainingDao trainingDao,
            TraineeDao traineeDao,
            TrainerDao trainerDao,
            AuthenticationService authenticationService
    ) {
        this.trainingDao = trainingDao;
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.authenticationService = authenticationService;
    }

    /** The caller has to be the trainee or the trainer of the new training. */
    public Training addTraining(Credentials credentials, AddTrainingRequest request) {
        authenticationService.authenticate(credentials);
        Trainee trainee = traineeDao.findByUsername(request.traineeUsername())
                .orElseThrow(() -> new EntityNotFoundException("Trainee", request.traineeUsername()));
        Trainer trainer = trainerDao.findByUsername(request.trainerUsername())
                .orElseThrow(() -> new EntityNotFoundException("Trainer", request.trainerUsername()));
        checkParticipant(credentials, trainee, trainer);

        Training training = new Training(
                trainee,
                trainer,
                request.trainingName(),
                trainer.getSpecialization(),
                request.trainingDate(),
                request.duration()
        );
        Training created = trainingDao.save(training);
        log.info("Added training '{}' for trainee '{}' with trainer '{}'",
                request.trainingName(), request.traineeUsername(), request.trainerUsername());
        return created;
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(Credentials credentials, TraineeTrainingCriteria criteria) {
        authenticationService.authenticateTrainee(credentials);
        checkDateRange(criteria.fromDate(), criteria.toDate());
        List<Training> trainings = trainingDao.findByTrainee(credentials.username(), criteria);
        log.debug("Found {} trainings of trainee '{}'", trainings.size(), credentials.username());
        return trainings;
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(Credentials credentials, TrainerTrainingCriteria criteria) {
        authenticationService.authenticateTrainer(credentials);
        checkDateRange(criteria.fromDate(), criteria.toDate());
        List<Training> trainings = trainingDao.findByTrainer(credentials.username(), criteria);
        log.debug("Found {} trainings of trainer '{}'", trainings.size(), credentials.username());
        return trainings;
    }

    private void checkParticipant(Credentials credentials, Trainee trainee, Trainer trainer) {
        String caller = credentials.username();
        boolean isTrainee = caller.equals(trainee.getUser().getUsername());
        boolean isTrainer = caller.equals(trainer.getUser().getUsername());
        if (!isTrainee && !isTrainer) {
            log.warn("User '{}' tried to add a training they do not take part in", caller);
            throw new AccessDeniedException("Only the trainee or the trainer can add this training");
        }
    }

    /** A rule across two filters, which bean validation on single fields cannot express. */
    private void checkDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidRequestException("From date must not be after to date");
        }
    }
}
