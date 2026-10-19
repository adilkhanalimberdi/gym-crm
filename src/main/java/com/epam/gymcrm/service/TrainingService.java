package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    public Training create(Long traineeId, Long trainerId, String trainingName,
                           TrainingType trainingType, LocalDateTime trainingDate, int durationMinutes) {
        log.info("Creating training '{}' for trainee {} with trainer {}", trainingName, traineeId, trainerId);
        if (traineeDao.findById(traineeId).isEmpty()) {
            log.warn("Cannot create training: trainee {} not found", traineeId);
            throw new EntityNotFoundException("Trainee", traineeId);
        }
        if (trainerDao.findById(trainerId).isEmpty()) {
            log.warn("Cannot create training: trainer {} not found", trainerId);
            throw new EntityNotFoundException("Trainer", trainerId);
        }
        Training created = trainingDao.create(
                new Training(null, traineeId, trainerId, trainingName, trainingType, trainingDate, durationMinutes));
        log.info("Created training with id {}", created.id());
        return created;
    }

    public Training findById(Long id) {
        log.debug("Selecting training with id {}", id);
        return trainingDao.findById(id).orElseThrow(() -> new EntityNotFoundException("Training", id));
    }

    public List<Training> findAll() {
        log.debug("Selecting all trainings");
        return trainingDao.findAll();
    }

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }
}
