package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class MappersTest {

    private static final Credentials GENERATED = new Credentials("John.Smith1", "abcde12345");

    @Mock
    private TrainingType yoga;

    @Test
    void traineeMapper_buildsActiveTraineeWithGeneratedCredentials() {
        var request = new CreateTraineeRequest(" John ", "Smith ", LocalDate.of(1990, 5, 14), "Street 1");

        Trainee trainee = new TraineeMapper().toEntity(request, GENERATED);

        assertNull(trainee.getId());
        assertEquals("John", trainee.getUser().getFirstName());
        assertEquals("Smith", trainee.getUser().getLastName());
        assertEquals("John.Smith1", trainee.getUser().getUsername());
        assertEquals("abcde12345", trainee.getUser().getPassword());
        assertTrue(trainee.getUser().isActive());
        assertEquals(LocalDate.of(1990, 5, 14), trainee.getDateOfBirth());
        assertEquals("Street 1", trainee.getAddress());
    }

    @Test
    void trainerMapper_buildsActiveTrainerWithGivenSpecialization() {
        var request = new CreateTrainerRequest("Anna", "Lee", "Yoga");

        Trainer trainer = new TrainerMapper().toEntity(request, yoga, new Credentials("Anna.Lee", "pw"));

        assertNull(trainer.getId());
        assertEquals("Anna", trainer.getUser().getFirstName());
        assertEquals("Anna.Lee", trainer.getUser().getUsername());
        assertTrue(trainer.getUser().isActive());
        assertSame(yoga, trainer.getSpecialization());
    }
}
