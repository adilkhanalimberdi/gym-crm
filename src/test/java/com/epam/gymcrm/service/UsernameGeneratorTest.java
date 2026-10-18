package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {

    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;

    private UsernameGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new UsernameGenerator();
        generator.setTraineeDao(traineeDao);
        generator.setTrainerDao(trainerDao);
    }

    @Test
    void generate_joinsFirstAndLastNameWithDot_whenFree() {
        assertEquals("John.Smith", generator.generate("John", "Smith"));
    }

    @Test
    void generate_appendsSerialOne_whenTakenByTrainee() {
        when(traineeDao.existsByUsername("John.Smith")).thenReturn(true);

        assertEquals("John.Smith1", generator.generate("John", "Smith"));
    }

    @Test
    void generate_appendsSerialOne_whenTakenByTrainer() {
        when(trainerDao.existsByUsername("John.Smith")).thenReturn(true);

        assertEquals("John.Smith1", generator.generate("John", "Smith"));
    }

    @Test
    void generate_skipsTakenSerials() {
        Set<String> traineeNames = Set.of("John.Smith", "John.Smith2");
        Set<String> trainerNames = Set.of("John.Smith1");
        when(traineeDao.existsByUsername(anyString())).thenAnswer(inv -> traineeNames.contains(inv.<String>getArgument(0)));
        when(trainerDao.existsByUsername(anyString())).thenAnswer(inv -> trainerNames.contains(inv.<String>getArgument(0)));

        assertEquals("John.Smith3", generator.generate("John", "Smith"));
    }
}
