package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidRequestException;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private TrainingTypeDao trainingTypeDao;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;
    @Mock
    private TrainingType yoga;
    @Mock
    private TrainingType zumba;
    @InjectMocks
    private TrainerService service;

    private Trainer trainer;

    @BeforeEach
    void setUp() {
        trainer = new Trainer(new User("Anna", "Lee", "Anna.Lee", "secret"), yoga);
    }

    @Test
    void create_generatesUsernameAndPasswordAndSavesActiveTrainer() {
        when(trainingTypeDao.findByName("Yoga")).thenReturn(Optional.of(yoga));
        when(usernameGenerator.generate("Anna", "Lee")).thenReturn("Anna.Lee");
        when(passwordGenerator.generate()).thenReturn("abcde12345");
        when(trainerDao.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainer created = service.create("Anna", "Lee", "Yoga");

        assertEquals("Anna.Lee", created.getUser().getUsername());
        assertEquals("abcde12345", created.getUser().getPassword());
        assertTrue(created.getUser().isActive());
        assertSame(yoga, created.getSpecialization());
    }

    @Test
    void create_rejectsMissingFirstName() {
        assertThrows(InvalidRequestException.class, () -> service.create(null, "Lee", "Yoga"));
        verifyNoInteractions(trainerDao);
    }

    @Test
    void create_rejectsMissingSpecialization() {
        assertThrows(InvalidRequestException.class, () -> service.create("Anna", "Lee", " "));
        verifyNoInteractions(trainerDao);
    }

    @Test
    void create_rejectsUnknownSpecialization() {
        when(trainingTypeDao.findByName("Chess")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.create("Anna", "Lee", "Chess"));
        verifyNoInteractions(trainerDao);
    }

    @Test
    void credentialsMatch_delegatesToAuthentication() {
        when(authenticationService.matchesTrainer(CREDENTIALS)).thenReturn(false);

        assertFalse(service.credentialsMatch(CREDENTIALS));
    }

    @Test
    void getProfile_returnsAuthenticatedTrainer() {
        when(authenticationService.authenticateTrainer(CREDENTIALS)).thenReturn(trainer);

        assertSame(trainer, service.getProfile(CREDENTIALS));
    }

    @Test
    void changePassword_storesNewPassword() {
        when(authenticationService.authenticateTrainer(CREDENTIALS)).thenReturn(trainer);

        service.changePassword(CREDENTIALS, "newSecret");

        assertEquals("newSecret", trainer.getUser().getPassword());
    }

    @Test
    void changePassword_rejectsMissingPassword() {
        when(authenticationService.authenticateTrainer(CREDENTIALS)).thenReturn(trainer);

        assertThrows(InvalidRequestException.class, () -> service.changePassword(CREDENTIALS, null));
    }

    @Test
    void update_changesNameAndSpecialization() {
        when(authenticationService.authenticateTrainer(CREDENTIALS)).thenReturn(trainer);
        when(trainingTypeDao.findByName("Zumba")).thenReturn(Optional.of(zumba));
        when(trainerDao.update(trainer)).thenReturn(trainer);

        Trainer updated = service.update(CREDENTIALS, "Annie", "Leigh", "Zumba");

        assertEquals("Annie", updated.getUser().getFirstName());
        assertEquals("Leigh", updated.getUser().getLastName());
        assertSame(zumba, updated.getSpecialization());
    }

    @Test
    void update_rejectsMissingFirstName_withoutSaving() {
        when(authenticationService.authenticateTrainer(CREDENTIALS)).thenReturn(trainer);

        assertThrows(InvalidRequestException.class, () -> service.update(CREDENTIALS, "", "Leigh", "Zumba"));
        verify(trainerDao, never()).update(any());
    }

    @Test
    void toggleActivation_flipsStateEachTime() {
        when(authenticationService.authenticateTrainer(CREDENTIALS)).thenReturn(trainer);

        assertFalse(service.toggleActivation(CREDENTIALS));
        assertTrue(service.toggleActivation(CREDENTIALS));
    }
}
