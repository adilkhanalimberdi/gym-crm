package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.TrainingType;

import java.util.Optional;

/** Read-only access: the list of training types cannot be changed from the application. */
public interface TrainingTypeDao {

    Optional<TrainingType> findByName(String name);
}
