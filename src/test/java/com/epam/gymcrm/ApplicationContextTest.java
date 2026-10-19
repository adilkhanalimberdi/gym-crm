package com.epam.gymcrm;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/** Boots the real Spring context to verify wiring, seed loading and username uniqueness end to end. */
class ApplicationContextTest {

    @Test
    void contextStartsAndLoadsSeedData() {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymFacade facade = context.getBean(GymFacade.class);

            assertEquals(2, facade.getAllTrainees().size());
            assertEquals(2, facade.getAllTrainers().size());
            assertEquals(2, facade.getAllTrainings().size());
        }
    }

    @Test
    void eachEntityTypeHasItsOwnStorageBean() {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            Map<Long, Trainee> trainees = context.getBean("traineeStorage", Map.class);
            Map<Long, Trainer> trainers = context.getBean("trainerStorage", Map.class);

            assertNotSame(trainees, trainers);
        }
    }

    @Test
    void usernamesStayUniqueAcrossTraineesAndTrainers() {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymFacade facade = context.getBean(GymFacade.class);

            // John.Smith already exists in the seed data as a trainee
            Trainee second = facade.createTrainee("John", "Smith", LocalDate.of(1992, 7, 20), "Road");
            Trainer third = facade.createTrainer("John", "Smith", TrainingType.CARDIO);
            Trainee fourth = facade.createTrainee("John", "Smith", LocalDate.of(1993, 1, 1), "Lane");

            assertEquals("John.Smith1", second.username());
            assertEquals("John.Smith2", third.username());
            assertEquals("John.Smith3", fourth.username());
            assertEquals(10, second.password().length());
        }
    }
}
