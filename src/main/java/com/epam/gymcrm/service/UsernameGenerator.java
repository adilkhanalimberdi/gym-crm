package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.exception.DuplicateUsernameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UsernameGenerator {

    private static final Logger log = LoggerFactory.getLogger(UsernameGenerator.class);

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    /**
     * Builds "First.Last"; if that is taken by any trainee or trainer, appends the first free
     * serial number ("First.Last1", "First.Last2", ...).
     * This is only a suggestion: uniqueness is enforced by the DAOs when the user is saved.
     */
    public String generate(String firstName, String lastName) {
        String base = firstName.trim() + "." + lastName.trim();
        String candidate = base;
        for (int serial = 1; isTaken(candidate); serial++) {
            candidate = base + serial;
        }
        return candidate;
    }

    /**
     * Generates a username and saves the user with it. If someone else took the same username in
     * between (the DAO rejects it), generates the next free one and tries again.
     */
    public <T> T createWithUniqueUsername(String firstName, String lastName, Function<String, T> save) {
        while (true) {
            String username = generate(firstName, lastName);
            try {
                return save.apply(username);
            } catch (DuplicateUsernameException e) {
                log.debug("Username {} was taken concurrently, generating another one", username);
            }
        }
    }

    private boolean isTaken(String username) {
        return traineeDao.existsByUsername(username) || trainerDao.existsByUsername(username);
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }
}
