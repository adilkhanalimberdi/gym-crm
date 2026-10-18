package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.UserDao;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.exception.AuthenticationFailedException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

/** Checks username/password pairs; every service call except profile creation starts here. */
@Service
@Transactional(readOnly = true)
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserDao userDao;
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;

    public AuthenticationService(UserDao userDao, TraineeDao traineeDao, TrainerDao trainerDao) {
        this.userDao = userDao;
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
    }

    public User authenticate(Credentials credentials) {
        return orElseFail(findUser(credentials), credentials);
    }

    public Trainee authenticateTrainee(Credentials credentials) {
        return orElseFail(findTrainee(credentials), credentials);
    }

    public Trainer authenticateTrainer(Credentials credentials) {
        return orElseFail(findTrainer(credentials), credentials);
    }

    public boolean matchesTrainee(Credentials credentials) {
        return findTrainee(credentials).isPresent();
    }

    public boolean matchesTrainer(Credentials credentials) {
        return findTrainer(credentials).isPresent();
    }

    private Optional<User> findUser(Credentials credentials) {
        return userDao.findByUsername(credentials.username())
                .filter(user -> passwordMatches(user, credentials));
    }

    private Optional<Trainee> findTrainee(Credentials credentials) {
        return traineeDao.findByUsername(credentials.username())
                .filter(trainee -> passwordMatches(trainee.getUser(), credentials));
    }

    private Optional<Trainer> findTrainer(Credentials credentials) {
        return trainerDao.findByUsername(credentials.username())
                .filter(trainer -> passwordMatches(trainer.getUser(), credentials));
    }

    private <T> T orElseFail(Optional<T> match, Credentials credentials) {
        return match.orElseThrow(() -> {
            log.warn("Authentication failed for username '{}'", credentials.username());
            return new AuthenticationFailedException();
        });
    }

    private boolean passwordMatches(User user, Credentials credentials) {
        if (credentials.password() == null) {
            return false;
        }
        // constant-time comparison, so response time does not reveal how much of the password matched
        return MessageDigest.isEqual(
                user.getPassword().getBytes(StandardCharsets.UTF_8),
                credentials.password().getBytes(StandardCharsets.UTF_8));
    }
}
