package com.epam.gymcrm;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class GymCrmApplication {

    private static final Logger log = LoggerFactory.getLogger(GymCrmApplication.class);

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymFacade facade = context.getBean(GymFacade.class);

            log.info("Seeded trainees: {}", facade.getAllTrainees());
            log.info("Seeded trainers: {}", facade.getAllTrainers());
            log.info("Seeded trainings: {}", facade.getAllTrainings());

            Trainee trainee = facade.createTrainee("John", "Smith", LocalDate.of(1992, 7, 20), "7 River Road");
            Trainer trainer = facade.createTrainer("Anna", "Lee", TrainingType.CARDIO);
            facade.createTraining(trainee.id(), trainer.id(), "Evening Cardio",
                    TrainingType.CARDIO, LocalDateTime.now().plusDays(1), 45);

            log.info("Trainees now: {}", facade.getAllTrainees());
            log.info("Trainers now: {}", facade.getAllTrainers());
            log.info("Trainings now: {}", facade.getAllTrainings());
        }
    }
}
