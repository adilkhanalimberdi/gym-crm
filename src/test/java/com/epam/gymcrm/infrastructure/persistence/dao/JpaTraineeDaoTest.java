package com.epam.gymcrm.infrastructure.persistence.dao;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.exception.ResourceNotFoundException;
import com.epam.gymcrm.infrastructure.persistence.entity.TraineeEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainerEntity;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.support.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JpaTraineeDaoTest extends PersistenceTest {

    @Autowired
    private TraineeDao traineeDao;
    @Autowired
    private TrainerDao trainerDao;

    @Test
    void save_persistsTraineeTogetherWithItsUser() {
        Trainee trainee = new Trainee(new User("John", "Smith", "John.Smith", "pw"), LocalDate.of(1990, 5, 14), "Street 1");

        Trainee saved = traineeDao.save(trainee);
        flushAndClear();

        assertNotNull(saved.getId());
        assertNotNull(saved.getUser().getId());
        Trainee loaded = traineeDao.findByUsername("John.Smith").orElseThrow();
        assertEquals("Street 1", loaded.getAddress());
        assertEquals(LocalDate.of(1990, 5, 14), loaded.getDateOfBirth());
        assertEquals("John", loaded.getUser().getFirstName());
        assertTrue(loaded.getUser().isActive());
    }

    @Test
    void findByUsername_returnsEmpty_whenUnknown() {
        assertTrue(traineeDao.findByUsername("Nobody.Here").isEmpty());
    }

    @Test
    void findByUsername_loadsAssignedTrainers() {
        TraineeEntity trainee = persistTrainee("John", "Smith");
        trainee.replaceTrainers(List.of(persistTrainer("Anna", "Lee", "Yoga"), persistTrainer("Mark", "Taylor", "Fitness")));
        flushAndClear();

        Trainee loaded = traineeDao.findByUsername("John.Smith").orElseThrow();

        assertEquals(2, loaded.getTrainers().size());
    }

    @Test
    void update_storesChangedFields() {
        persistTrainee("John", "Smith");
        flushAndClear();
        Trainee loaded = traineeDao.findByUsername("John.Smith").orElseThrow();
        loaded.setAddress("New Address");
        loaded.getUser().setFirstName("Johnny");
        loaded.getUser().setPassword("newSecret");
        loaded.getUser().toggleActive();

        traineeDao.update(loaded);
        flushAndClear();

        Trainee stored = traineeDao.findByUsername("John.Smith").orElseThrow();
        assertEquals("New Address", stored.getAddress());
        assertEquals("Johnny", stored.getUser().getFirstName());
        assertEquals("newSecret", stored.getUser().getPassword());
        assertFalse(stored.getUser().isActive());
    }

    @Test
    void update_replacesAssignedTrainers() {
        TraineeEntity trainee = persistTrainee("John", "Smith");
        trainee.replaceTrainers(List.of(persistTrainer("Anna", "Lee", "Yoga")));
        persistTrainer("Mark", "Taylor", "Fitness");
        flushAndClear();
        Trainer mark = trainerDao.findByUsername("Mark.Taylor").orElseThrow();
        Trainee loaded = traineeDao.findByUsername("John.Smith").orElseThrow();
        loaded.replaceTrainers(List.of(mark));

        traineeDao.update(loaded);
        flushAndClear();

        assertEquals(List.of(mark), List.copyOf(traineeDao.findByUsername("John.Smith").orElseThrow().getTrainers()));
    }

    @Test
    void update_fails_whenTraineeNoLongerExists() {
        Trainee missing = new Trainee(-1L, new User("No", "One", "No.One", "pw"), null, null, List.of());

        assertThrows(ResourceNotFoundException.class, () -> traineeDao.update(missing));
    }

    @Test
    void delete_removesUserAndTrainingsButKeepsTrainers() {
        TraineeEntity trainee = persistTrainee("John", "Smith");
        TrainerEntity trainer = persistTrainer("Anna", "Lee", "Yoga");
        trainee.replaceTrainers(List.of(trainer));
        persistTraining(trainee, trainer, "Morning Yoga", "Yoga", LocalDate.of(2025, 3, 1));
        persistTraining(trainee, trainer, "Evening Yoga", "Yoga", LocalDate.of(2025, 3, 2));
        flushAndClear();

        traineeDao.delete(traineeDao.findByUsername("John.Smith").orElseThrow());
        flushAndClear();

        assertTrue(traineeDao.findByUsername("John.Smith").isEmpty());
        assertEquals(0, count("select count(u) from UserEntity u where u.username = 'John.Smith'"));
        assertEquals(0, count("select count(t) from TrainingEntity t"));
        assertEquals(0, ((Number) entityManager.createNativeQuery("select count(*) from trainee_trainer")
                .getSingleResult()).longValue());
        assertEquals(1, count("select count(t) from TrainerEntity t"));
    }

    @Test
    void delete_fails_whenTraineeNoLongerExists() {
        Trainee missing = new Trainee(-1L, new User("No", "One", "No.One", "pw"), null, null, List.of());

        assertThrows(ResourceNotFoundException.class, () -> traineeDao.delete(missing));
    }
}
