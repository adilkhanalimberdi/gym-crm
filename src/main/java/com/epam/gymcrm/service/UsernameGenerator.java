package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsernameGenerator {

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    /**
     * Builds "First.Last"; if that is taken by any trainee or trainer, appends the first free
     * serial number ("First.Last1", "First.Last2", ...).
     * Callers creating users concurrently should synchronize on this bean around generate + save.
     */
    public String generate(String firstName, String lastName) {
        String base = firstName.trim() + "." + lastName.trim();
        String candidate = base;
        for (int serial = 1; isTaken(candidate); serial++) {
            candidate = base + serial;
        }
        return candidate;
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
