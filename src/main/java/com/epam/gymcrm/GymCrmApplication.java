package com.epam.gymcrm;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.NewTraining;
import com.epam.gymcrm.dto.TraineeTrainingCriteria;
import com.epam.gymcrm.exception.AuthenticationFailedException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;

/** Small demo that walks through the main use cases against the in-memory H2 database. */
public class GymCrmApplication {

    private static final Logger log = LoggerFactory.getLogger(GymCrmApplication.class);

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymFacade facade = context.getBean(GymFacade.class);

            Trainer trainer = facade.createTrainer("Anna", "Lee", "Yoga");
            Trainee trainee = facade.createTrainee("John", "Smith", LocalDate.of(1992, 7, 20), "7 River Road");
            Credentials trainerCredentials = new Credentials(trainer.getUser().getUsername(), trainer.getUser().getPassword());
            Credentials traineeCredentials = new Credentials(trainee.getUser().getUsername(), trainee.getUser().getPassword());

            log.info("Trainee credentials match: {}", facade.traineeCredentialsMatch(traineeCredentials));
            log.info("Unassigned trainers: {}", facade.getUnassignedTrainers(traineeCredentials));

            facade.updateTraineeTrainers(traineeCredentials, List.of(trainer.getUser().getUsername()));
            log.info("Unassigned trainers after assigning: {}", facade.getUnassignedTrainers(traineeCredentials));

            facade.addTraining(traineeCredentials, new NewTraining(trainee.getUser().getUsername(),
                    trainer.getUser().getUsername(), "Morning Yoga", "Yoga", LocalDate.now().plusDays(1), 60));
            log.info("Trainee trainings: {}",
                    facade.getTraineeTrainings(traineeCredentials, TraineeTrainingCriteria.none()));

            log.info("Trainee active after toggle: {}", facade.toggleTraineeActivation(traineeCredentials));
            log.info("Trainer profile: {}", facade.getTrainerProfile(trainerCredentials));

            facade.deleteTrainee(traineeCredentials);
            try {
                facade.getTraineeProfile(traineeCredentials);
            } catch (AuthenticationFailedException e) {
                log.info("Trainee is gone after delete: {}", e.getMessage());
            }
        }
    }
}
