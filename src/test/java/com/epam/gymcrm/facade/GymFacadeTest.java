package com.epam.gymcrm.facade;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    private static final LocalDate BIRTH = LocalDate.of(1990, 1, 1);
    private static final LocalDateTime WHEN = LocalDateTime.of(2025, 3, 1, 9, 0);

    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private TrainingService trainingService;
    @InjectMocks
    private GymFacade facade;

    private final Trainee trainee = new Trainee(1L, "John", "Smith", "John.Smith", "pw", true, BIRTH, "Street");
    private final Trainer trainer = new Trainer(2L, "Anna", "Lee", "Anna.Lee", "pw", true, TrainingType.YOGA);
    private final Training training = new Training(3L, 1L, 2L, "Yoga", TrainingType.YOGA, WHEN, 60);

    @Test
    void traineeOperations_delegateToTraineeService() {
        when(traineeService.create("John", "Smith", BIRTH, "Street")).thenReturn(trainee);
        when(traineeService.update(trainee)).thenReturn(trainee);
        when(traineeService.findById(1L)).thenReturn(trainee);
        when(traineeService.findAll()).thenReturn(List.of(trainee));

        assertEquals(trainee, facade.createTrainee("John", "Smith", BIRTH, "Street"));
        assertEquals(trainee, facade.updateTrainee(trainee));
        assertEquals(trainee, facade.getTrainee(1L));
        assertEquals(List.of(trainee), facade.getAllTrainees());
        facade.deleteTrainee(1L);
        verify(traineeService).delete(1L);
    }

    @Test
    void trainerOperations_delegateToTrainerService() {
        when(trainerService.create("Anna", "Lee", TrainingType.YOGA)).thenReturn(trainer);
        when(trainerService.update(trainer)).thenReturn(trainer);
        when(trainerService.findById(2L)).thenReturn(trainer);
        when(trainerService.findAll()).thenReturn(List.of(trainer));

        assertEquals(trainer, facade.createTrainer("Anna", "Lee", TrainingType.YOGA));
        assertEquals(trainer, facade.updateTrainer(trainer));
        assertEquals(trainer, facade.getTrainer(2L));
        assertEquals(List.of(trainer), facade.getAllTrainers());
    }

    @Test
    void trainingOperations_delegateToTrainingService() {
        when(trainingService.create(1L, 2L, "Yoga", TrainingType.YOGA, WHEN, 60)).thenReturn(training);
        when(trainingService.findById(3L)).thenReturn(training);
        when(trainingService.findAll()).thenReturn(List.of(training));

        assertEquals(training, facade.createTraining(1L, 2L, "Yoga", TrainingType.YOGA, WHEN, 60));
        assertEquals(training, facade.getTraining(3L));
        assertEquals(List.of(training), facade.getAllTrainings());
    }
}
