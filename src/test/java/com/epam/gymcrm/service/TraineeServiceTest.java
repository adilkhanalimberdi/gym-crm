package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    private static final Trainee TRAINEE =
            new Trainee(1L, "John", "Smith", "John.Smith", "pw", true, LocalDate.of(1990, 1, 1), "Street 1");

    @Mock
    private TraineeDao traineeDao;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;

    private TraineeService service;

    @BeforeEach
    void setUp() {
        service = new TraineeService();
        service.setTraineeDao(traineeDao);
        service.setUsernameGenerator(usernameGenerator);
        service.setPasswordGenerator(passwordGenerator);
    }

    @Test
    void create_assignsGeneratedUsernameAndPasswordAndActivates() {
        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");
        when(passwordGenerator.generate()).thenReturn("abcde12345");
        when(traineeDao.create(any(Trainee.class))).thenAnswer(inv -> ((Trainee) inv.getArgument(0)).withId(7L));

        Trainee created = service.create("John", "Smith", LocalDate.of(1990, 1, 1), "Street 1");

        ArgumentCaptor<Trainee> saved = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeDao).create(saved.capture());
        assertEquals("John.Smith", saved.getValue().username());
        assertEquals("abcde12345", saved.getValue().password());
        assertTrue(saved.getValue().active());
        assertEquals(7L, created.id());
    }

    @Test
    void update_returnsTrainee_whenExists() {
        when(traineeDao.update(TRAINEE)).thenReturn(true);

        assertEquals(TRAINEE, service.update(TRAINEE));
    }

    @Test
    void update_throws_whenMissing() {
        when(traineeDao.update(TRAINEE)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.update(TRAINEE));
    }

    @Test
    void delete_delegatesToDao() {
        when(traineeDao.deleteById(1L)).thenReturn(true);

        service.delete(1L);

        verify(traineeDao).deleteById(1L);
    }

    @Test
    void delete_throws_whenMissing() {
        when(traineeDao.deleteById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.delete(1L));
    }

    @Test
    void findById_returnsTrainee_whenExists() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(TRAINEE));

        assertEquals(TRAINEE, service.findById(1L));
    }

    @Test
    void findById_throws_whenMissing() {
        when(traineeDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void findAll_returnsAllTrainees() {
        when(traineeDao.findAll()).thenReturn(List.of(TRAINEE));

        assertEquals(List.of(TRAINEE), service.findAll());
    }
}
