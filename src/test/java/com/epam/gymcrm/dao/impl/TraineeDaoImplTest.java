package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraineeDaoImplTest {

    private Map<Long, Trainee> storage;
    private TraineeDaoImpl dao;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        dao = new TraineeDaoImpl();
        dao.setStorage(storage);
    }

    private static Trainee trainee(Long id, String username) {
        return new Trainee(id, "John", "Smith", username, "pw", true, LocalDate.of(1990, 1, 1), "Street");
    }

    @Test
    void create_assignsIncrementingIds() {
        Trainee first = dao.create(trainee(null, "a"));
        Trainee second = dao.create(trainee(null, "b"));

        assertEquals(1L, first.id());
        assertEquals(2L, second.id());
        assertEquals(2, storage.size());
    }

    @Test
    void create_continuesAfterHighestSeededId() {
        storage.put(5L, trainee(5L, "seed"));
        dao.setStorage(storage);

        assertEquals(6L, dao.create(trainee(null, "new")).id());
    }

    @Test
    void findById_returnsStoredTrainee() {
        Trainee created = dao.create(trainee(null, "a"));

        assertEquals(created, dao.findById(created.id()).orElseThrow());
        assertTrue(dao.findById(99L).isEmpty());
    }

    @Test
    void findAll_returnsEveryTrainee() {
        dao.create(trainee(null, "a"));
        dao.create(trainee(null, "b"));

        assertEquals(2, dao.findAll().size());
    }

    @Test
    void update_replacesExisting() {
        Trainee created = dao.create(trainee(null, "a"));
        Trainee changed = new Trainee(created.id(), "Jane", "Smith", "a", "pw", false, null, "New");

        assertTrue(dao.update(changed));
        assertEquals(changed, dao.findById(created.id()).orElseThrow());
    }

    @Test
    void update_returnsFalse_whenMissingOrIdNull() {
        assertFalse(dao.update(trainee(42L, "x")));
        assertFalse(dao.update(trainee(null, "x")));
        assertTrue(storage.isEmpty());
    }

    @Test
    void deleteById_removesExisting() {
        Trainee created = dao.create(trainee(null, "a"));

        assertTrue(dao.deleteById(created.id()));
        assertTrue(storage.isEmpty());
    }

    @Test
    void deleteById_returnsFalse_whenMissingOrIdNull() {
        assertFalse(dao.deleteById(42L));
        assertFalse(dao.deleteById(null));
    }

    @Test
    void existsByUsername_matchesExactUsername() {
        dao.create(trainee(null, "John.Smith"));

        assertTrue(dao.existsByUsername("John.Smith"));
        assertFalse(dao.existsByUsername("John.Smith1"));
    }
}
