package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    private static final Trainer TRAINER = new Trainer(1L, "Anna", "Lee", "Anna.Lee", "pw", true, TrainingType.YOGA);

    @Mock
    private TrainerDao trainerDao;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;

    private TrainerService service;

    @BeforeEach
    void setUp() {
        service = new TrainerService();
        service.setTrainerDao(trainerDao);
        service.setUsernameGenerator(usernameGenerator);
        service.setPasswordGenerator(passwordGenerator);
        service.setTrainerMapper(new TrainerMapper());
    }

    @Test
    @SuppressWarnings("unchecked")
    void create_assignsGeneratedUsernameAndPasswordAndActivates() {
        when(usernameGenerator.createWithUniqueUsername(eq("Anna"), eq("Lee"), any()))
                .thenAnswer(inv -> ((Function<String, Object>) inv.getArgument(2)).apply("Anna.Lee1"));
        when(passwordGenerator.generate()).thenReturn("zzzzz99999");
        when(trainerDao.create(any(Trainer.class))).thenAnswer(inv -> ((Trainer) inv.getArgument(0)).withId(3L));

        Trainer created = service.create(new CreateTrainerRequest("Anna", "Lee", TrainingType.YOGA));

        ArgumentCaptor<Trainer> saved = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerDao).create(saved.capture());
        assertEquals("Anna.Lee1", saved.getValue().username());
        assertEquals("zzzzz99999", saved.getValue().password());
        assertTrue(saved.getValue().active());
        assertEquals(TrainingType.YOGA, saved.getValue().specialization());
        assertEquals(3L, created.id());
    }

    @Test
    void update_returnsTrainer_whenExists() {
        when(trainerDao.update(TRAINER)).thenReturn(true);

        assertEquals(TRAINER, service.update(TRAINER));
    }

    @Test
    void update_throws_whenMissing() {
        when(trainerDao.update(TRAINER)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.update(TRAINER));
    }

    @Test
    void findById_returnsTrainer_whenExists() {
        when(trainerDao.findById(1L)).thenReturn(Optional.of(TRAINER));

        assertEquals(TRAINER, service.findById(1L));
    }

    @Test
    void findById_throws_whenMissing() {
        when(trainerDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void findAll_returnsAllTrainers() {
        when(trainerDao.findAll()).thenReturn(List.of(TRAINER));

        assertEquals(List.of(TRAINER), service.findAll());
    }
}
