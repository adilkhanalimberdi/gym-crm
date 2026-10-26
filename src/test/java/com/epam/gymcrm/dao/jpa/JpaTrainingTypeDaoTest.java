package com.epam.gymcrm.dao.jpa;

import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.support.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JpaTrainingTypeDaoTest extends PersistenceTest {

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    @Test
    void findById_returnsTypeLoadedFromImportScript() {
        Long yogaId = trainingType("Yoga").getId();

        assertEquals("Yoga", trainingTypeDao.findById(yogaId).orElseThrow().getName());
    }

    @Test
    void findById_returnsEmpty_forUnknownId() {
        assertTrue(trainingTypeDao.findById(-1L).isEmpty());
    }

    @Test
    void findAll_returnsAllTypesFromImportScript() {
        List<String> names = trainingTypeDao.findAll().stream().map(TrainingType::getName).toList();

        assertEquals(List.of("Fitness", "Yoga", "Zumba", "Stretching", "Resistance"), names);
    }
}
