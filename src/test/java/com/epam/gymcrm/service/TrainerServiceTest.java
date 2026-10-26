package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.TrainerProfile;
import com.epam.gymcrm.dto.request.CreateTrainerRequest;
import com.epam.gymcrm.dto.request.UpdateTrainerRequest;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidRequestException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    private static final Credentials CREDENTIALS = new Credentials("Anna.Lee", "secret");

    @Mock
    private TrainerDao trainerDao;
    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainingTypeDao trainingTypeDao;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;
    @Spy
    private TrainerMapper mapper = new TrainerMapper();
    @Mock
    private TrainingType yoga;
    @InjectMocks
    private TrainerService service;

    private Trainer trainer;

    @BeforeEach
    void setUp() {
        trainer = new Trainer(new User("Anna", "Lee", "Anna.Lee", "secret"), yoga);
    }

    @Test
    void create_generatesUsernameAndPasswordAndSavesActiveTrainer() {
        when(trainingTypeDao.findById(2L)).thenReturn(Optional.of(yoga));
        when(usernameGenerator.generate("Anna", "Lee")).thenReturn("Anna.Lee");
        when(passwordGenerator.generate()).thenReturn("abcde12345");
        when(trainerDao.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainer created = service.create(new CreateTrainerRequest("Anna", "Lee", 2L));

        assertEquals("Anna.Lee", created.getUser().getUsername());
        assertEquals("abcde12345", created.getUser().getPassword());
        assertTrue(created.getUser().isActive());
        assertSame(yoga, created.getSpecialization());
    }

    @Test
    void create_rejectsMissingFirstName() {
        assertThrows(InvalidRequestException.class, () -> service.create(new CreateTrainerRequest(null, "Lee", 2L)));
        verifyNoInteractions(trainerDao);
    }

    @Test
    void create_rejectsMissingSpecialization() {
        assertThrows(InvalidRequestException.class, () -> service.create(new CreateTrainerRequest("Anna", "Lee", null)));
        verifyNoInteractions(trainerDao);
    }

    @Test
    void create_rejectsUnknownSpecialization() {
        when(trainingTypeDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.create(new CreateTrainerRequest("Anna", "Lee", 99L)));
        verifyNoInteractions(trainerDao);
    }

    @Test
    void getProfile_returnsAuthenticatedTrainerWithItsTrainees() {
        Trainee trainee = new Trainee(new User("John", "Smith", "John.Smith", "pw"), null, null);
        when(authenticationService.authenticateTrainer(CREDENTIALS)).thenReturn(trainer);
        when(traineeDao.findAllByTrainer("Anna.Lee")).thenReturn(List.of(trainee));

        TrainerProfile profile = service.getProfile(CREDENTIALS);

        assertSame(trainer, profile.trainer());
        assertEquals(List.of(trainee), profile.trainees());
    }

    @Test
    void update_changesNameAndActiveFlagAndKeepsSpecialization() {
        when(authenticationService.authenticateTrainer(CREDENTIALS)).thenReturn(trainer);
        when(trainerDao.update(trainer)).thenReturn(trainer);
        when(traineeDao.findAllByTrainer("Anna.Lee")).thenReturn(List.of());

        TrainerProfile updated = service.update(CREDENTIALS, new UpdateTrainerRequest("Annie", "Leigh", false));

        assertEquals("Annie", updated.trainer().getUser().getFirstName());
        assertEquals("Leigh", updated.trainer().getUser().getLastName());
        assertFalse(updated.trainer().getUser().isActive());
        assertSame(yoga, updated.trainer().getSpecialization());
    }

    @Test
    void update_rejectsMissingFirstName_withoutSaving() {
        when(authenticationService.authenticateTrainer(CREDENTIALS)).thenReturn(trainer);

        assertThrows(InvalidRequestException.class, () -> service.update(CREDENTIALS, new UpdateTrainerRequest("", "Leigh", true)));
        verify(trainerDao, never()).update(any());
    }
}
