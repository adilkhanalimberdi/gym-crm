package com.epam.gymcrm.facade;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.NewTraining;
import com.epam.gymcrm.dto.TraineeTrainingCriteria;
import com.epam.gymcrm.dto.TrainerTrainingCriteria;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
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

    // profile creation (no authentication needed)

    public Trainer createTrainer(String firstName, String lastName, String specializationName) {
        return trainerService.create(firstName, lastName, specializationName);
    }

    public Trainee createTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        return traineeService.create(firstName, lastName, dateOfBirth, address);
    }

    // everything below needs valid credentials and acts on the caller's own profile

    public boolean traineeCredentialsMatch(Credentials credentials) {
        return traineeService.credentialsMatch(credentials);
    }

    public boolean trainerCredentialsMatch(Credentials credentials) {
        return trainerService.credentialsMatch(credentials);
    }

    public Trainer getTrainerProfile(Credentials credentials) {
        return trainerService.getProfile(credentials);
    }

    public Trainee getTraineeProfile(Credentials credentials) {
        return traineeService.getProfile(credentials);
    }

    public void changeTraineePassword(Credentials credentials, String newPassword) {
        traineeService.changePassword(credentials, newPassword);
    }

    public void changeTrainerPassword(Credentials credentials, String newPassword) {
        trainerService.changePassword(credentials, newPassword);
    }

    public Trainer updateTrainer(Credentials credentials, String firstName, String lastName, String specializationName) {
        return trainerService.update(credentials, firstName, lastName, specializationName);
    }

    public Trainee updateTrainee(Credentials credentials, String firstName, String lastName,
                                 LocalDate dateOfBirth, String address) {
        return traineeService.update(credentials, firstName, lastName, dateOfBirth, address);
    }

    public boolean toggleTraineeActivation(Credentials credentials) {
        return traineeService.toggleActivation(credentials);
    }

    public boolean toggleTrainerActivation(Credentials credentials) {
        return trainerService.toggleActivation(credentials);
    }

    public void deleteTrainee(Credentials credentials) {
        traineeService.delete(credentials);
    }

    public List<Training> getTraineeTrainings(Credentials credentials, TraineeTrainingCriteria criteria) {
        return trainingService.getTraineeTrainings(credentials, criteria);
    }

    public List<Training> getTrainerTrainings(Credentials credentials, TrainerTrainingCriteria criteria) {
        return trainingService.getTrainerTrainings(credentials, criteria);
    }

    public Training addTraining(Credentials credentials, NewTraining training) {
        return trainingService.addTraining(credentials, training);
    }

    public List<Trainer> getUnassignedTrainers(Credentials credentials) {
        return traineeService.getUnassignedTrainers(credentials);
    }

    public Trainee updateTraineeTrainers(Credentials credentials, Collection<String> trainerUsernames) {
        return traineeService.updateTrainers(credentials, trainerUsernames);
    }
}
