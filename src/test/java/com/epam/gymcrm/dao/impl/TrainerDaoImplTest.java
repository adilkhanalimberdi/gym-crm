package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerDaoImplTest {

    private Map<Long, Trainer> storage;
    private TrainerDaoImpl dao;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        dao = new TrainerDaoImpl();
        dao.setStorage(storage);
    }

    private static Trainer trainer(Long id, String username) {
        return new Trainer(id, "Anna", "Lee", username, "pw", true, TrainingType.YOGA);
    }

    @Test
    void create_assignsIdAndStores() {
        Trainer created = dao.create(trainer(null, "Anna.Lee"));

        assertEquals(1L, created.id());
        assertEquals(created, dao.findById(1L).orElseThrow());
    }

    @Test
    void findAll_returnsEveryTrainer() {
        dao.create(trainer(null, "a"));
        dao.create(trainer(null, "b"));

        assertEquals(2, dao.findAll().size());
    }

    @Test
    void update_replacesExisting() {
        Trainer created = dao.create(trainer(null, "a"));
        Trainer changed = new Trainer(created.id(), "Anna", "Lee", "a", "pw", true, TrainingType.CARDIO);

        assertTrue(dao.update(changed));
        assertEquals(TrainingType.CARDIO, dao.findById(created.id()).orElseThrow().specialization());
    }

    @Test
    void update_returnsFalse_whenMissing() {
        assertFalse(dao.update(trainer(42L, "x")));
    }

    @Test
    void existsByUsername_matchesExactUsername() {
        dao.create(trainer(null, "Anna.Lee"));

        assertTrue(dao.existsByUsername("Anna.Lee"));
        assertFalse(dao.existsByUsername("Anna.Lee1"));
    }
}
