package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainee;

public interface TraineeDao extends Dao<Trainee> {

    /** Replaces an existing trainee; returns false if no trainee has that id. */
    boolean update(Trainee trainee);

    boolean deleteById(Long id);

    boolean existsByUsername(String username);
}
