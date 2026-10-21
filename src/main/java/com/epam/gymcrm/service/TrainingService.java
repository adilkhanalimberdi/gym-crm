package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.dto.request.CreateTrainingRequest;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TrainingMapper;
import com.epam.gymcrm.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    private TrainingMapper mapper;

    public Training create(CreateTrainingRequest request) {
        log.info("Creating training '{}' for trainee {} with trainer {}",
                request.name(),
                request.traineeId(),
                request.trainerId());

        if (traineeDao.findById(request.traineeId()).isEmpty()) {
            log.warn("Cannot create training: trainee {} not found", request.traineeId());
            throw new EntityNotFoundException("Trainee", request.traineeId());
        }
        if (trainerDao.findById(request.trainerId()).isEmpty()) {
            log.warn("Cannot create training: trainer {} not found", request.trainerId());
            throw new EntityNotFoundException("Trainer", request.trainerId());
        }
        Training created = trainingDao.create(
                mapper.toEntity(request)
        );

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

    @Autowired
    public void setTrainingMapper(TrainingMapper mapper) {
        this.mapper = mapper;
    }

}
