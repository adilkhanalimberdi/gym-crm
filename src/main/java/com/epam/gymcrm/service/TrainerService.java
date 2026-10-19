package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private TrainerDao trainerDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    public Trainer create(String firstName, String lastName, TrainingType specialization) {
        log.info("Creating trainer {} {}", firstName, lastName);
        // generator is shared with TraineeService, so it is the lock that keeps usernames unique
        synchronized (usernameGenerator) {
            String username = usernameGenerator.generate(firstName, lastName);
            Trainer trainer = new Trainer(null, firstName, lastName, username,
                    passwordGenerator.generate(), true, specialization);
            Trainer created = trainerDao.create(trainer);
            log.info("Created trainer {}", created);
            return created;
        }
    }

    public Trainer update(Trainer trainer) {
        log.info("Updating trainer with id {}", trainer.id());
        if (!trainerDao.update(trainer)) {
            log.warn("Cannot update trainer: id {} not found", trainer.id());
            throw new EntityNotFoundException("Trainer", trainer.id());
        }
        return trainer;
    }

    public Trainer findById(Long id) {
        log.debug("Selecting trainer with id {}", id);
        return trainerDao.findById(id).orElseThrow(() -> new EntityNotFoundException("Trainer", id));
    }

    public List<Trainer> findAll() {
        log.debug("Selecting all trainers");
        return trainerDao.findAll();
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }
}
