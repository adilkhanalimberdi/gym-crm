package com.epam.gymcrm.storage;

import com.epam.gymcrm.exception.DuplicateUsernameException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Persistence-level uniqueness of usernames across trainees and trainers, the in-memory
 * counterpart of a UNIQUE constraint: the "is it free?" check and the insert are one atomic step.
 * With a real database this class disappears and the constraint does the same job.
 */
@Component
public class UsernameIndex {

    private final Object lock = new Object();
    private final Map<Long, Trainee> traineeStorage;
    private final Map<Long, Trainer> trainerStorage;

    public UsernameIndex(
            @Qualifier(InMemoryStorageConfig.TRAINEE_STORAGE) Map<Long, Trainee> traineeStorage,
            @Qualifier(InMemoryStorageConfig.TRAINER_STORAGE) Map<Long, Trainer> trainerStorage
    ) {
        this.traineeStorage = traineeStorage;
        this.trainerStorage = trainerStorage;
    }

    /** Runs the insert only if no trainee or trainer has the username; otherwise throws DuplicateUsernameException. */
    public <T> T insertIfFree(String username, Supplier<T> insert) {
        synchronized (lock) {
            if (isTaken(username)) {
                throw new DuplicateUsernameException(username);
            }
            return insert.get();
        }
    }

    private boolean isTaken(String username) {
        return traineeStorage.values().stream().anyMatch(t -> t.username().equals(username))
                || trainerStorage.values().stream().anyMatch(t -> t.username().equals(username));
    }
}
