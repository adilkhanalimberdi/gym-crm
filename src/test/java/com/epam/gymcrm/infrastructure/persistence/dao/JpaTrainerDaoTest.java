package com.epam.gymcrm.infrastructure.persistence.dao;

import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.exception.ResourceNotFoundException;
import com.epam.gymcrm.infrastructure.persistence.entity.TraineeEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainerEntity;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.support.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JpaTrainerDaoTest extends PersistenceTest {

    @Autowired
    private TrainerDao trainerDao;
    @Autowired
    private TrainingTypeDao trainingTypeDao;

    private TrainingType domainTrainingType(String name) {
        return trainingTypeDao.findByName(name).orElseThrow();
    }

    @Test
    void save_persistsTrainerWithUserAndSpecialization() {
        Trainer saved = trainerDao.save(new Trainer(new User("Anna", "Lee", "Anna.Lee", "pw"), domainTrainingType("Yoga")));
        flushAndClear();

        assertNotNull(saved.getId());
        assertEquals("Yoga", saved.getSpecialization().getName());
        Trainer loaded = trainerDao.findByUsername("Anna.Lee").orElseThrow();
        assertEquals("Yoga", loaded.getSpecialization().getName());
        assertEquals("Lee", loaded.getUser().getLastName());
    }

    @Test
    void findByUsername_returnsEmpty_whenUnknown() {
        assertTrue(trainerDao.findByUsername("Nobody.Here").isEmpty());
    }

    @Test
    void update_storesNewSpecializationAndUserChanges() {
        persistTrainer("Anna", "Lee", "Yoga");
        flushAndClear();
        Trainer loaded = trainerDao.findByUsername("Anna.Lee").orElseThrow();
        loaded.setSpecialization(domainTrainingType("Zumba"));
        loaded.getUser().setPassword("newSecret");
        loaded.getUser().toggleActive();

        Trainer updated = trainerDao.update(loaded);
        flushAndClear();

        assertEquals("Zumba", updated.getSpecialization().getName());
        Trainer stored = trainerDao.findByUsername("Anna.Lee").orElseThrow();
        assertEquals("Zumba", stored.getSpecialization().getName());
        assertEquals("newSecret", stored.getUser().getPassword());
        assertFalse(stored.getUser().isActive());
    }

    @Test
    void update_fails_whenTrainerNoLongerExists() {
        Trainer missing = new Trainer(-1L, new User("No", "One", "No.One", "pw"), domainTrainingType("Yoga"));

        assertThrows(ResourceNotFoundException.class, () -> trainerDao.update(missing));
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
        TraineeEntity trainee = persistTrainee("John", "Smith");
        TrainerEntity assigned = persistTrainer("Anna", "Lee", "Yoga");
        persistTrainer("Mark", "Taylor", "Fitness");
        persistTrainer("Olga", "King", "Zumba");
        trainee.replaceTrainers(List.of(assigned));
        flushAndClear();

        List<Trainer> notAssigned = trainerDao.findNotAssignedToTrainee("John.Smith");

        assertEquals(List.of("Mark.Taylor", "Olga.King"),
                notAssigned.stream().map(t -> t.getUser().getUsername()).toList());
    }

    @Test
    void trainerSearches_sendOneStatement_withSeveralTrainers() {
        persistTrainee("John", "Smith");
        persistTrainer("Anna", "Lee", "Yoga");
        persistTrainer("Mark", "Taylor", "Fitness");
        flushAndClear();

        assertEquals(1, countStatements(() -> trainerDao.findNotAssignedToTrainee("John.Smith")));
        entityManager.clear();
        assertEquals(1, countStatements(() -> trainerDao.findAllByUsernames(Set.of("Anna.Lee", "Mark.Taylor"))));
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
