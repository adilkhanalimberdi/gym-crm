package com.epam.gymcrm.dao;

import com.epam.gymcrm.dto.PageQuery;
import com.epam.gymcrm.model.Trainer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TrainerDao {

    Trainer save(Trainer trainer);

    Trainer update(Trainer trainer);

    Optional<Trainer> findByUsername(String username);

    List<Trainer> findAllByUsernames(Collection<String> usernames);

    /** Trainers that are not in the given trainee's list of assigned trainers, ordered by username. */
    List<Trainer> findNotAssignedToTrainee(String traineeUsername, PageQuery page);
}
