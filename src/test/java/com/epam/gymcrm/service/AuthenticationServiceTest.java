package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.UserDao;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.exception.AuthenticationFailedException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private static final Credentials VALID = new Credentials("John.Smith", "secret");
    private static final Credentials WRONG_PASSWORD = new Credentials("John.Smith", "other");

    private final User user = new User("John", "Smith", "John.Smith", "secret");

    @Mock
    private UserDao userDao;
    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @InjectMocks
    private AuthenticationService service;

    @Test
    void authenticate_returnsUser_whenPasswordMatches() {
        when(userDao.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        assertSame(user, service.authenticate(VALID));
    }

    @Test
    void authenticate_fails_whenPasswordIsWrong() {
        when(userDao.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        assertThrows(AuthenticationFailedException.class, () -> service.authenticate(WRONG_PASSWORD));
    }

    @Test
    void authenticate_fails_whenPasswordIsMissing() {
        when(userDao.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        assertThrows(AuthenticationFailedException.class,
                () -> service.authenticate(new Credentials("John.Smith", null)));
    }

    @Test
    void authenticate_fails_whenUserIsUnknown() {
        when(userDao.findByUsername("John.Smith")).thenReturn(Optional.empty());

        assertThrows(AuthenticationFailedException.class, () -> service.authenticate(VALID));
    }

    @Test
    void authenticateTrainee_returnsTrainee_whenPasswordMatches() {
        Trainee trainee = new Trainee(user, null, null);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        assertSame(trainee, service.authenticateTrainee(VALID));
    }

    @Test
    void authenticateTrainee_fails_whenNoTraineeProfileExists() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.empty());

        assertThrows(AuthenticationFailedException.class, () -> service.authenticateTrainee(VALID));
    }

    @Test
    void authenticateTrainer_returnsTrainer_whenPasswordMatches() {
        Trainer trainer = new Trainer(user, mock(TrainingType.class));
        when(trainerDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainer));

        assertSame(trainer, service.authenticateTrainer(VALID));
    }

    @Test
    void authenticateTrainer_fails_whenPasswordIsWrong() {
        Trainer trainer = new Trainer(user, mock(TrainingType.class));
        when(trainerDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainer));

        assertThrows(AuthenticationFailedException.class, () -> service.authenticateTrainer(WRONG_PASSWORD));
    }
}
