package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {

    Trainee save(Trainee trainee);

    Trainee update(Trainee trainee);

    /** Hard delete; also removes the trainee's user and trainings. */
    void delete(Trainee trainee);

    Optional<Trainee> findByUsername(String username);

    /** Trainees that have the trainer with the given username in their list of assigned trainers. */
    List<Trainee> findAllByTrainer(String trainerUsername);
}
