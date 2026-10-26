package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.NewTraining;
import com.epam.gymcrm.dto.TraineeTrainingCriteria;
import com.epam.gymcrm.dto.TrainerTrainingCriteria;
import com.epam.gymcrm.exception.AccessDeniedException;
import com.epam.gymcrm.exception.AuthenticationFailedException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidRequestException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    private static final LocalDate DATE = LocalDate.of(2025, 3, 1);
    private static final Credentials TRAINEE_CREDENTIALS = new Credentials("John.Smith", "pw1");
    private static final Credentials TRAINER_CREDENTIALS = new Credentials("Anna.Lee", "pw2");
    private static final NewTraining REQUEST =
            new NewTraining("John.Smith", "Anna.Lee", "Morning Yoga", DATE, 60);

    @Mock
    private TrainingDao trainingDao;
    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private TrainingType yoga;
    @InjectMocks
    private TrainingService service;

    private Trainee trainee;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        trainee = new Trainee(new User("John", "Smith", "John.Smith", "pw1"), null, null);
        trainer = new Trainer(new User("Anna", "Lee", "Anna.Lee", "pw2"), yoga);
    }

    private void stubExistingParticipants() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("Anna.Lee")).thenReturn(Optional.of(trainer));
    }

    @Test
    void addTraining_savesTrainingOfTrainerSpecialization_whenCalledByTrainee() {
        stubExistingParticipants();
        when(trainingDao.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

        Training created = service.addTraining(TRAINEE_CREDENTIALS, REQUEST);

        assertSame(trainee, created.getTrainee());
        assertSame(trainer, created.getTrainer());
        assertSame(yoga, created.getTrainingType());
        assertEquals("Morning Yoga", created.getTrainingName());
        assertEquals(DATE, created.getTrainingDate());
        assertEquals(60, created.getDuration());
    }

    @Test
    void addTraining_savesTraining_whenCalledByTrainer() {
        stubExistingParticipants();
        when(trainingDao.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

        Training created = service.addTraining(TRAINER_CREDENTIALS, REQUEST);

        assertEquals("Morning Yoga", created.getTrainingName());
    }

    @Test
    void addTraining_isDenied_whenCallerTakesNoPartInTheTraining() {
        stubExistingParticipants();

        assertThrows(AccessDeniedException.class,
                () -> service.addTraining(new Credentials("Eve.Stranger", "pw"), REQUEST));
        verify(trainingDao, never()).save(any());
    }

    @Test
    void addTraining_failsWhenAuthenticationFails() {
        when(authenticationService.authenticate(TRAINEE_CREDENTIALS)).thenThrow(new AuthenticationFailedException());

        assertThrows(AuthenticationFailedException.class, () -> service.addTraining(TRAINEE_CREDENTIALS, REQUEST));
        verifyNoInteractions(trainingDao, traineeDao);
    }

    @Test
    void addTraining_failsForUnknownTrainee() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.addTraining(TRAINEE_CREDENTIALS, REQUEST));
    }

    @Test
    void addTraining_failsForUnknownTrainer() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("Anna.Lee")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.addTraining(TRAINEE_CREDENTIALS, REQUEST));
    }

    @Test
    void addTraining_validatesRequiredFieldsBeforeTouchingTheDatabase() {
        assertThrows(InvalidRequestException.class, () -> service.addTraining(TRAINEE_CREDENTIALS,
                new NewTraining("John.Smith", "Anna.Lee", " ", DATE, 60)));
        assertThrows(InvalidRequestException.class, () -> service.addTraining(TRAINEE_CREDENTIALS,
                new NewTraining("John.Smith", "Anna.Lee", "Name", null, 60)));
        assertThrows(InvalidRequestException.class, () -> service.addTraining(TRAINEE_CREDENTIALS,
                new NewTraining("John.Smith", "Anna.Lee", "Name", DATE, 0)));

        verifyNoInteractions(authenticationService, traineeDao, trainerDao, trainingDao);
    }

    @Test
    void getTraineeTrainings_returnsTrainingsFromDao() {
        var criteria = new TraineeTrainingCriteria(DATE, DATE.plusDays(5), "Anna", "Yoga");
        List<Training> trainings = List.of(new Training(trainee, trainer, "Yoga", yoga, DATE, 60));
        when(trainingDao.findByTrainee("John.Smith", criteria)).thenReturn(trainings);

        assertEquals(trainings, service.getTraineeTrainings(TRAINEE_CREDENTIALS, criteria));
        verify(authenticationService).authenticateTrainee(TRAINEE_CREDENTIALS);
    }

    @Test
    void getTraineeTrainings_rejectsInvertedDateRange() {
        var criteria = new TraineeTrainingCriteria(DATE.plusDays(1), DATE, null, null);

        assertThrows(InvalidRequestException.class, () -> service.getTraineeTrainings(TRAINEE_CREDENTIALS, criteria));
        verifyNoInteractions(trainingDao);
    }

    @Test
    void getTrainerTrainings_returnsTrainingsFromDao() {
        var criteria = new TrainerTrainingCriteria(null, null, "John");
        List<Training> trainings = List.of(new Training(trainee, trainer, "Yoga", yoga, DATE, 60));
        when(trainingDao.findByTrainer("Anna.Lee", criteria)).thenReturn(trainings);

        assertEquals(trainings, service.getTrainerTrainings(TRAINER_CREDENTIALS, criteria));
        verify(authenticationService).authenticateTrainer(TRAINER_CREDENTIALS);
    }

    @Test
    void getTrainerTrainings_rejectsInvertedDateRange() {
        var criteria = new TrainerTrainingCriteria(DATE.plusDays(1), DATE, null);

        assertThrows(InvalidRequestException.class, () -> service.getTrainerTrainings(TRAINER_CREDENTIALS, criteria));
        verifyNoInteractions(trainingDao);
    }
}
