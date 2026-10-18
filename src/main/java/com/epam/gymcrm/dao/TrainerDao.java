package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainer;

public interface TrainerDao extends Dao<Trainer> {

    /** Replaces an existing trainer; returns false if no trainer has that id. */
    boolean update(Trainer trainer);

    boolean existsByUsername(String username);
}
