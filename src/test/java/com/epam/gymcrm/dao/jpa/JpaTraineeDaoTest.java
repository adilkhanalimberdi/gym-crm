package com.epam.gymcrm.dao.jpa;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.support.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JpaTraineeDaoTest extends PersistenceTest {

    @Autowired
    private TraineeDao traineeDao;

    @Test
    void save_persistsTraineeTogetherWithItsUser() {
        Trainee trainee = new Trainee(new User("John", "Smith", "John.Smith", "pw"), LocalDate.of(1990, 5, 14), "Street 1");

        traineeDao.save(trainee);
        flushAndClear();

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
    void update_storesChangedFields() {
        persistTrainee("John", "Smith");
        flushAndClear();
        Trainee detached = traineeDao.findByUsername("John.Smith").orElseThrow();
        entityManager.detach(detached);
        detached.setAddress("New Address");

        traineeDao.update(detached);
        flushAndClear();

        assertEquals("New Address", traineeDao.findByUsername("John.Smith").orElseThrow().getAddress());
    }

    @Test
    void findAllByTrainer_returnsOnlyTraineesAssignedToThatTrainer() {
        Trainer anna = persistTrainer("Anna", "Lee", "Yoga");
        Trainer mark = persistTrainer("Mark", "Taylor", "Fitness");
        persistTrainee("John", "Smith").replaceTrainers(List.of(anna));
        persistTrainee("Kate", "Brown").replaceTrainers(List.of(anna, mark));
        persistTrainee("Paul", "White").replaceTrainers(List.of(mark));
        flushAndClear();

        List<Trainee> found = traineeDao.findAllByTrainer("Anna.Lee");

        assertEquals(List.of("John.Smith", "Kate.Brown"), found.stream().map(t -> t.getUser().getUsername()).toList());
    }

    @Test
    void delete_removesUserAndTrainingsButKeepsTrainers() {
        Trainee trainee = persistTrainee("John", "Smith");
        Trainer trainer = persistTrainer("Anna", "Lee", "Yoga");
        trainee.replaceTrainers(List.of(trainer));
        persistTraining(trainee, trainer, "Morning Yoga", "Yoga", LocalDate.of(2025, 3, 1));
        persistTraining(trainee, trainer, "Evening Yoga", "Yoga", LocalDate.of(2025, 3, 2));
        flushAndClear();

        traineeDao.delete(traineeDao.findByUsername("John.Smith").orElseThrow());
        flushAndClear();

        assertTrue(traineeDao.findByUsername("John.Smith").isEmpty());
        assertEquals(0, count("select count(u) from User u where u.username = 'John.Smith'"));
        assertEquals(0, count("select count(t) from Training t"));
        assertEquals(0, ((Number) entityManager.createNativeQuery("select count(*) from trainee_trainer")
                .getSingleResult()).longValue());
        assertEquals(1, count("select count(t) from Trainer t"));
    }

    @Test
    void delete_acceptsDetachedTrainee() {
        persistTrainee("John", "Smith");
        flushAndClear();
        Trainee detached = traineeDao.findByUsername("John.Smith").orElseThrow();
        entityManager.detach(detached);

        traineeDao.delete(detached);
        flushAndClear();

        assertTrue(traineeDao.findByUsername("John.Smith").isEmpty());
    }
}
