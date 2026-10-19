package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    private static final LocalDateTime DATE = LocalDateTime.of(2025, 3, 1, 9, 0);
    private static final Training TRAINING = new Training(1L, 1L, 2L, "Yoga", TrainingType.YOGA, DATE, 60);

    @Mock
    private TrainingDao trainingDao;
    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @Mock
    private Trainee trainee;
    @Mock
    private Trainer trainer;

    private TrainingService service;

    @BeforeEach
    void setUp() {
        service = new TrainingService();
        service.setTrainingDao(trainingDao);
        service.setTraineeDao(traineeDao);
        service.setTrainerDao(trainerDao);
    }

    @Test
    void create_storesTraining_whenTraineeAndTrainerExist() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerDao.findById(2L)).thenReturn(Optional.of(trainer));
        when(trainingDao.create(any(Training.class))).thenAnswer(inv -> ((Training) inv.getArgument(0)).withId(1L));

        Training created = service.create(1L, 2L, "Yoga", TrainingType.YOGA, DATE, 60);

        assertEquals(TRAINING, created);
    }

    @Test
    void create_throws_whenTraineeMissing() {
        when(traineeDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.create(1L, 2L, "Yoga", TrainingType.YOGA, DATE, 60));
        verify(trainingDao, never()).create(any());
    }

    @Test
    void create_throws_whenTrainerMissing() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerDao.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.create(1L, 2L, "Yoga", TrainingType.YOGA, DATE, 60));
        verify(trainingDao, never()).create(any());
    }

    @Test
    void findById_returnsTraining_whenExists() {
        when(trainingDao.findById(1L)).thenReturn(Optional.of(TRAINING));

        assertEquals(TRAINING, service.findById(1L));
    }

    @Test
    void findById_throws_whenMissing() {
        when(trainingDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void findAll_returnsAllTrainings() {
        when(trainingDao.findAll()).thenReturn(List.of(TRAINING));

        assertEquals(List.of(TRAINING), service.findAll());
    }
}
