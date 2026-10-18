package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingDaoImplTest {

    private TrainingDaoImpl dao;

    @BeforeEach
    void setUp() {
        dao = new TrainingDaoImpl();
        dao.setStorage(new HashMap<>());
    }

    private static Training training() {
        return new Training(null, 1L, 2L, "Yoga", TrainingType.YOGA, LocalDateTime.of(2025, 3, 1, 9, 0), 60);
    }

    @Test
    void create_assignsIdAndStores() {
        Training created = dao.create(training());

        assertEquals(1L, created.id());
        assertEquals(created, dao.findById(1L).orElseThrow());
    }

    @Test
    void findById_returnsEmpty_whenMissing() {
        assertTrue(dao.findById(5L).isEmpty());
    }

    @Test
    void findAll_returnsEveryTraining() {
        dao.create(training());
        dao.create(training());

        assertEquals(2, dao.findAll().size());
    }
}
