package com.epam.gymcrm.facade;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.NewTraining;
import com.epam.gymcrm.dto.TraineeTrainingCriteria;
import com.epam.gymcrm.dto.TrainerTrainingCriteria;
import com.epam.gymcrm.exception.AccessDeniedException;
import com.epam.gymcrm.exception.AuthenticationFailedException;
import com.epam.gymcrm.exception.InvalidRequestException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.support.PersistenceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** End-to-end scenarios through the facade, real services and the H2 database. */
class GymFacadeTest extends PersistenceTest {

    private static final LocalDate DATE = LocalDate.of(2025, 3, 1);

    @Autowired
    private GymFacade facade;

    private Trainee trainee;
    private Trainer trainer;
    private Credentials traineeCredentials;
    private Credentials trainerCredentials;

    @BeforeEach
    void createProfiles() {
        trainee = facade.createTrainee("John", "Smith", LocalDate.of(1990, 5, 14), "Street 1");
        trainer = facade.createTrainer("Anna", "Lee", "Yoga");
        traineeCredentials = new Credentials(trainee.getUser().getUsername(), trainee.getUser().getPassword());
        trainerCredentials = new Credentials(trainer.getUser().getUsername(), trainer.getUser().getPassword());
    }

    private NewTraining newTraining(String name, LocalDate date) {
        return new NewTraining(traineeCredentials.username(), trainerCredentials.username(), name, "Yoga", date, 60);
    }

    @Test
    void createProfiles_generateUsernameAndTenCharacterPassword() {
        assertEquals("John.Smith", trainee.getUser().getUsername());
        assertEquals("Anna.Lee", trainer.getUser().getUsername());
        assertEquals(10, trainee.getUser().getPassword().length());
        assertTrue(trainee.getUser().isActive());
        assertNotNull(trainee.getId());
    }

    @Test
    void createProfile_addsSerialNumber_whenNameIsTakenByTraineeOrTrainer() {
        Trainee second = facade.createTrainee("John", "Smith", null, null);
        Trainer third = facade.createTrainer("John", "Smith", "Fitness");

        assertEquals("John.Smith1", second.getUser().getUsername());
        assertEquals("John.Smith2", third.getUser().getUsername());
    }

    @Test
    void createProfile_validatesRequiredFields() {
        assertThrows(InvalidRequestException.class, () -> facade.createTrainee("", "Smith", null, null));
        assertThrows(InvalidRequestException.class, () -> facade.createTrainer("Anna", "Lee", null));
    }

    @Test
    void credentialsMatch_checksUsernameAndPassword() {
        assertTrue(facade.traineeCredentialsMatch(traineeCredentials));
        assertTrue(facade.trainerCredentialsMatch(trainerCredentials));
        assertFalse(facade.traineeCredentialsMatch(new Credentials(traineeCredentials.username(), "wrong")));
        assertFalse(facade.trainerCredentialsMatch(traineeCredentials));
    }

    @Test
    void protectedOperations_failWithWrongCredentials() {
        Credentials wrong = new Credentials(traineeCredentials.username(), "wrong");

        assertThrows(AuthenticationFailedException.class, () -> facade.getTraineeProfile(wrong));
        assertThrows(AuthenticationFailedException.class, () -> facade.deleteTrainee(wrong));
        assertThrows(AuthenticationFailedException.class, () -> facade.getTrainerProfile(traineeCredentials));
    }

    @Test
    void getProfile_returnsOwnProfile() {
        assertEquals("Street 1", facade.getTraineeProfile(traineeCredentials).getAddress());
        assertEquals("Yoga", facade.getTrainerProfile(trainerCredentials).getSpecialization().getName());
    }

    @Test
    void changePassword_replacesPasswordForLaterLogins() {
        facade.changeTraineePassword(traineeCredentials, "newPass123");
        facade.changeTrainerPassword(trainerCredentials, "newPass456");

        assertTrue(facade.traineeCredentialsMatch(new Credentials(traineeCredentials.username(), "newPass123")));
        assertFalse(facade.traineeCredentialsMatch(traineeCredentials));
        assertTrue(facade.trainerCredentialsMatch(new Credentials(trainerCredentials.username(), "newPass456")));
    }

    @Test
    void updateProfiles_storeNewValues() {
        facade.updateTrainee(traineeCredentials, "Johnny", "Smythe", LocalDate.of(1985, 1, 1), "Other Street");
        facade.updateTrainer(trainerCredentials, "Annie", "Leigh", "Zumba");
        flushAndClear();

        Trainee updatedTrainee = facade.getTraineeProfile(traineeCredentials);
        assertEquals("Johnny", updatedTrainee.getUser().getFirstName());
        assertEquals("Other Street", updatedTrainee.getAddress());
        assertEquals("Zumba", facade.getTrainerProfile(trainerCredentials).getSpecialization().getName());
    }

    @Test
    void toggleActivation_isNotIdempotent() {
        assertFalse(facade.toggleTraineeActivation(traineeCredentials));
        assertTrue(facade.toggleTraineeActivation(traineeCredentials));
        assertFalse(facade.toggleTrainerActivation(trainerCredentials));
        assertTrue(facade.toggleTrainerActivation(trainerCredentials));
    }

    @Test
    void updateTraineeTrainers_changesWhichTrainersAreUnassigned() {
        Trainer second = facade.createTrainer("Mark", "Taylor", "Fitness");
        assertEquals(2, facade.getUnassignedTrainers(traineeCredentials).size());

        facade.updateTraineeTrainers(traineeCredentials, Set.of(trainer.getUser().getUsername()));
        flushAndClear();

        List<Trainer> unassigned = facade.getUnassignedTrainers(traineeCredentials);
        assertEquals(List.of(second.getUser().getUsername()), unassigned.stream().map(t -> t.getUser().getUsername()).toList());

        facade.updateTraineeTrainers(traineeCredentials, Set.of());
        flushAndClear();
        assertEquals(2, facade.getUnassignedTrainers(traineeCredentials).size());
    }

    @Test
    void addTraining_isListedForTraineeAndTrainerWithFilters() {
        facade.addTraining(traineeCredentials, newTraining("Morning Yoga", DATE));
        facade.addTraining(trainerCredentials, newTraining("Evening Yoga", DATE.plusDays(10)));
        flushAndClear();

        List<Training> all = facade.getTraineeTrainings(traineeCredentials, TraineeTrainingCriteria.none());
        List<Training> late = facade.getTraineeTrainings(traineeCredentials,
                new TraineeTrainingCriteria(DATE.plusDays(5), null, "Anna", "Yoga"));
        List<Training> forTrainer = facade.getTrainerTrainings(trainerCredentials,
                new TrainerTrainingCriteria(null, null, "John"));

        assertEquals(2, all.size());
        assertEquals("Evening Yoga", late.get(0).getTrainingName());
        assertEquals(2, forTrainer.size());
    }

    @Test
    void addTraining_isDenied_forSomeoneWhoDoesNotTakePart() {
        Trainee stranger = facade.createTrainee("Eve", "Stranger", null, null);
        Credentials strangerCredentials = new Credentials(stranger.getUser().getUsername(), stranger.getUser().getPassword());

        assertThrows(AccessDeniedException.class,
                () -> facade.addTraining(strangerCredentials, newTraining("Sneaky", DATE)));
    }

    @Test
    void deleteTrainee_removesProfileAndCascadesToTrainings() {
        facade.updateTraineeTrainers(traineeCredentials, Set.of(trainer.getUser().getUsername()));
        facade.addTraining(traineeCredentials, newTraining("Morning Yoga", DATE));
        flushAndClear();

        facade.deleteTrainee(traineeCredentials);
        flushAndClear();

        assertFalse(facade.traineeCredentialsMatch(traineeCredentials));
        assertEquals(0, count("select count(t) from Training t"));
        assertEquals(0, count("select count(u) from User u where u.username = 'John.Smith'"));
        assertTrue(facade.trainerCredentialsMatch(trainerCredentials));
        assertEquals(List.of(), facade.getTrainerTrainings(trainerCredentials, TrainerTrainingCriteria.none()));
    }
}
