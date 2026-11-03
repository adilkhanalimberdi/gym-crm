package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainee;

import java.util.Optional;

public interface TraineeDao {

    Trainee save(Trainee trainee);

    Trainee update(Trainee trainee);

    /** Hard delete; also removes the trainee's user and trainings. */
    void delete(Trainee trainee);

    Optional<Trainee> findByUsername(String username);
}
