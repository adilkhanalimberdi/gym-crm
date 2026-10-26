package com.epam.gymcrm.dao.jpa;

import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.support.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JpaTrainerDaoTest extends PersistenceTest {

    @Autowired
    private TrainerDao trainerDao;

    @Test
    void save_persistsTrainerWithUserAndSpecialization() {
        TrainingType yoga = trainingType("Yoga");

        trainerDao.save(new Trainer(new User("Anna", "Lee", "Anna.Lee", "pw"), yoga));
        flushAndClear();

        Trainer loaded = trainerDao.findByUsername("Anna.Lee").orElseThrow();
        assertEquals("Yoga", loaded.getSpecialization().getName());
        assertEquals("Lee", loaded.getUser().getLastName());
    }

    @Test
    void findByUsername_returnsEmpty_whenUnknown() {
        assertTrue(trainerDao.findByUsername("Nobody.Here").isEmpty());
    }

    @Test
    void update_storesNewSpecialization() {
        persistTrainer("Anna", "Lee", "Yoga");
        flushAndClear();
        Trainer detached = trainerDao.findByUsername("Anna.Lee").orElseThrow();
        entityManager.detach(detached);
        detached.setSpecialization(trainingType("Zumba"));

        trainerDao.update(detached);
        flushAndClear();

        assertEquals("Zumba", trainerDao.findByUsername("Anna.Lee").orElseThrow().getSpecialization().getName());
    }

    @Test
    void findAllByUsernames_returnsOnlyRequestedTrainers() {
        persistTrainer("Anna", "Lee", "Yoga");
        persistTrainer("Mark", "Taylor", "Fitness");
        persistTrainer("Olga", "King", "Zumba");

        List<Trainer> found = trainerDao.findAllByUsernames(Set.of("Anna.Lee", "Olga.King", "Missing.Person"));

        assertEquals(Set.of("Anna.Lee", "Olga.King"),
                Set.copyOf(found.stream().map(t -> t.getUser().getUsername()).toList()));
    }

    @Test
    void findAllByUsernames_returnsEmptyList_forNoUsernames() {
        assertTrue(trainerDao.findAllByUsernames(Set.of()).isEmpty());
    }

    @Test
    void findNotAssignedToTrainee_excludesAssignedTrainers() {
        Trainee trainee = persistTrainee("John", "Smith");
        Trainer assigned = persistTrainer("Anna", "Lee", "Yoga");
        persistTrainer("Mark", "Taylor", "Fitness");
        persistTrainer("Olga", "King", "Zumba");
        trainee.replaceTrainers(List.of(assigned));
        flushAndClear();

        List<Trainer> notAssigned = trainerDao.findNotAssignedToTrainee("John.Smith");

        assertEquals(List.of("Mark.Taylor", "Olga.King"),
                notAssigned.stream().map(t -> t.getUser().getUsername()).toList());
    }

    @Test
    void findNotAssignedToTrainee_excludesInactiveTrainers() {
        persistTrainee("John", "Smith");
        persistTrainer("Anna", "Lee", "Yoga");
        persistTrainer("Mark", "Taylor", "Fitness").getUser().setActive(false);
        flushAndClear();

        List<Trainer> notAssigned = trainerDao.findNotAssignedToTrainee("John.Smith");

        assertEquals(List.of("Anna.Lee"), notAssigned.stream().map(t -> t.getUser().getUsername()).toList());
    }

    @Test
    void findNotAssignedToTrainee_returnsAllTrainers_whenNothingAssigned() {
        persistTrainee("John", "Smith");
        persistTrainer("Anna", "Lee", "Yoga");
        persistTrainer("Mark", "Taylor", "Fitness");
        flushAndClear();

        assertEquals(2, trainerDao.findNotAssignedToTrainee("John.Smith").size());
    }
}
