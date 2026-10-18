package com.epam.gymcrm.dao;

import java.util.List;
import java.util.Optional;

public interface Dao<T> {

    /** Stores a new entity, assigning it the next free id, and returns the stored entity. */
    T create(T entity);

    Optional<T> findById(Long id);

    List<T> findAll();
}
