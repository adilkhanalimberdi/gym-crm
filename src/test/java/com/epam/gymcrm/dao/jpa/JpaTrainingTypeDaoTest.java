package com.epam.gymcrm.dao.jpa;

import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.support.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JpaTrainingTypeDaoTest extends PersistenceTest {

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    @Test
    void findByName_returnsTypeLoadedFromImportScript() {
        assertEquals("Yoga", trainingTypeDao.findByName("Yoga").orElseThrow().getName());
    }

    @Test
    void findByName_returnsEmpty_forUnknownType() {
        assertTrue(trainingTypeDao.findByName("Chess").isEmpty());
    }
}
