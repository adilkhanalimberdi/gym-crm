package com.epam.gymcrm.facade;

import com.epam.gymcrm.config.AppConfig;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.NewTraining;
import com.epam.gymcrm.dto.PageQuery;
import com.epam.gymcrm.dto.TraineeTrainingCriteria;
import com.epam.gymcrm.dto.TrainerTrainingCriteria;
import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reads results the way a real caller does: after the service transaction has ended. PersistenceTest keeps one
 * transaction open for the whole test, which would hide lazy loading failures. These tests commit, so the
 * context (and with it the database schema) is rebuilt after each one.
 */
@SpringJUnitConfig(AppConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GymFacadeOutsideTransactionTest {

    private static final LocalDate DATE = LocalDate.of(2025, 3, 1);

    @Autowired
    private GymFacade facade;

    private Credentials traineeCredentials;
    private Credentials trainerCredentials;

    @BeforeEach
    void createProfiles() {
        Trainee trainee = facade.createTrainee(new CreateTraineeRequest("John", "Smith", null, null));
        Trainer trainer = facade.createTrainer(new CreateTrainerRequest("Anna", "Lee", "Yoga"));
        traineeCredentials = new Credentials(trainee.getUser().getUsername(), trainee.getUser().getPassword());
        trainerCredentials = new Credentials(trainer.getUser().getUsername(), trainer.getUser().getPassword());
    }

    private void assignTrainer() {
        facade.updateTraineeTrainers(traineeCredentials, List.of(trainerCredentials.username()));
    }

    @Test
    void getTraineeProfile_returnsAssignedTrainers() {
        assignTrainer();

        Set<Trainer> trainers = facade.getTraineeProfile(traineeCredentials).getTrainers();

        assertEquals(1, trainers.size());
        Trainer trainer = trainers.iterator().next();
        assertEquals("Anna.Lee", trainer.getUser().getUsername());
        assertEquals("Yoga", trainer.getSpecialization().getName());
    }

    @Test
    void updateTraineeTrainers_returnsAssignedTrainers() {
        Trainee updated = facade.updateTraineeTrainers(traineeCredentials, List.of(trainerCredentials.username()));

        Trainer trainer = updated.getTrainers().iterator().next();
        assertEquals("Anna", trainer.getUser().getFirstName());
        assertEquals("Yoga", trainer.getSpecialization().getName());
    }

    @Test
    void getTrainerProfile_returnsUserAndSpecialization() {
        Trainer trainer = facade.getTrainerProfile(trainerCredentials);

        assertEquals("Lee", trainer.getUser().getLastName());
        assertEquals("Yoga", trainer.getSpecialization().getName());
    }

    @Test
    void getUnassignedTrainers_returnsUserAndSpecialization() {
        Trainer trainer = facade.getUnassignedTrainers(traineeCredentials, PageQuery.firstPage()).get(0);

        assertEquals("Anna.Lee", trainer.getUser().getUsername());
        assertEquals("Yoga", trainer.getSpecialization().getName());
    }

    @Test
    void trainingSearches_returnParticipantsAndType() {
        Training added = facade.addTraining(traineeCredentials, new NewTraining(
                traineeCredentials.username(),
                trainerCredentials.username(),
                "Morning Yoga",
                "Yoga",
                DATE,
                60));

        Training ofTrainee = facade.getTraineeTrainings(traineeCredentials, TraineeTrainingCriteria.none(), PageQuery.firstPage()).get(0);
        Training ofTrainer = facade.getTrainerTrainings(trainerCredentials, TrainerTrainingCriteria.none(), PageQuery.firstPage()).get(0);

        assertEquals(added.getId(), ofTrainee.getId());
        assertEquals("Anna", ofTrainee.getTrainer().firstName());
        assertEquals("Yoga", ofTrainee.getTrainingType().getName());
        assertEquals("John", ofTrainer.getTrainee().firstName());
    }

    /** Services no longer work on managed entities, so a change has to be stored explicitly. */
    @Test
    void changes_arePersistedWhenTheTransactionEnds() {
        assertFalse(facade.toggleTraineeActivation(traineeCredentials));
        assertFalse(facade.getTraineeProfile(traineeCredentials).getUser().isActive());
        assertFalse(facade.toggleTrainerActivation(trainerCredentials));
        assertFalse(facade.getTrainerProfile(trainerCredentials).getUser().isActive());

        facade.changeTraineePassword(traineeCredentials, "newPass123");
        facade.changeTrainerPassword(trainerCredentials, "newPass456");

        assertTrue(facade.traineeCredentialsMatch(new Credentials(traineeCredentials.username(), "newPass123")));
        assertTrue(facade.trainerCredentialsMatch(new Credentials(trainerCredentials.username(), "newPass456")));
    }

    @Test
    void deleteTrainee_removesProfileAndTrainings() {
        assignTrainer();
        facade.addTraining(traineeCredentials, new NewTraining(
                traineeCredentials.username(),
                trainerCredentials.username(),
                "Morning Yoga",
                "Yoga",
                DATE,
                60));

        facade.deleteTrainee(traineeCredentials);

        assertFalse(facade.traineeCredentialsMatch(traineeCredentials));
        assertTrue(facade.getTrainerTrainings(trainerCredentials, TrainerTrainingCriteria.none(), PageQuery.firstPage()).isEmpty());
    }
}
