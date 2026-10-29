package com.epam.gymcrm.facade;

import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.CreateTrainingRequest;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import org.springframework.stereotype.Component;

import java.util.List;

/** Single entry point to the gym CRM; services are injected through the constructor. */
@Component
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    public Trainee createTrainee(CreateTraineeRequest request) {
        return traineeService.create(request);
    }

    public Trainee updateTrainee(Trainee trainee) {
        return traineeService.update(trainee);
    }

    public void deleteTrainee(Long id) {
        traineeService.delete(id);
    }

    public Trainee getTrainee(Long id) {
        return traineeService.findById(id);
    }

    public List<Trainee> getAllTrainees() {
        return traineeService.findAll();
    }

    public Trainer createTrainer(CreateTrainerRequest request) {
        return trainerService.create(request);
    }

    public Trainer updateTrainer(Trainer trainer) {
        return trainerService.update(trainer);
    }

    public Trainer getTrainer(Long id) {
        return trainerService.findById(id);
    }

    public List<Trainer> getAllTrainers() {
        return trainerService.findAll();
    }

    public Training createTraining(CreateTrainingRequest request) {
        return trainingService.create(request);
    }

    public Training getTraining(Long id) {
        return trainingService.findById(id);
    }

    public List<Training> getAllTrainings() {
        return trainingService.findAll();
    }
}
