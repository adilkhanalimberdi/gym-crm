package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private TraineeDao traineeDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    public Trainee create(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        log.info("Creating trainee {} {}", firstName, lastName);
        // generator is shared with TrainerService, so it is the lock that keeps usernames unique
        synchronized (usernameGenerator) {
            String username = usernameGenerator.generate(firstName, lastName);
            Trainee trainee = new Trainee(null, firstName, lastName, username,
                    passwordGenerator.generate(), true, dateOfBirth, address);
            Trainee created = traineeDao.create(trainee);
            log.info("Created trainee {}", created);
            return created;
        }
    }

    public Trainee update(Trainee trainee) {
        log.info("Updating trainee with id {}", trainee.id());
        if (!traineeDao.update(trainee)) {
            log.warn("Cannot update trainee: id {} not found", trainee.id());
            throw new EntityNotFoundException("Trainee", trainee.id());
        }
        return trainee;
    }

    public void delete(Long id) {
        log.info("Deleting trainee with id {}", id);
        if (!traineeDao.deleteById(id)) {
            log.warn("Cannot delete trainee: id {} not found", id);
            throw new EntityNotFoundException("Trainee", id);
        }
    }

    public Trainee findById(Long id) {
        log.debug("Selecting trainee with id {}", id);
        return traineeDao.findById(id).orElseThrow(() -> new EntityNotFoundException("Trainee", id));
    }

    public List<Trainee> findAll() {
        log.debug("Selecting all trainees");
        return traineeDao.findAll();
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
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
