package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.exception.AuthenticationFailedException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.InvalidRequestException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    private static final Credentials CREDENTIALS = new Credentials("John.Smith", "secret");

    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;
    @InjectMocks
    private TraineeService service;

    private Trainee trainee;

    @BeforeEach
    void setUp() {
        trainee = new Trainee(new User("John", "Smith", "John.Smith", "secret"), LocalDate.of(1990, 1, 1), "Street 1");
    }

    private Trainer trainerWithUsername(String username) {
        return new Trainer(new User("T", "T", username, "pw"), mock(TrainingType.class));
    }

    @Test
    void create_generatesUsernameAndPasswordAndSavesActiveTrainee() {
        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith1");
        when(passwordGenerator.generate()).thenReturn("abcde12345");
        when(traineeDao.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainee created = service.create(" John ", "Smith", LocalDate.of(1990, 1, 1), "Street 1");

        assertEquals("John.Smith1", created.getUser().getUsername());
        assertEquals("abcde12345", created.getUser().getPassword());
        assertEquals("John", created.getUser().getFirstName());
        assertTrue(created.getUser().isActive());
        assertEquals("Street 1", created.getAddress());
    }

    @Test
    void create_rejectsMissingFirstName() {
        assertThrows(InvalidRequestException.class, () -> service.create(" ", "Smith", null, null));
        verifyNoInteractions(traineeDao, usernameGenerator);
    }

    @Test
    void create_rejectsMissingLastName() {
        assertThrows(InvalidRequestException.class, () -> service.create("John", null, null, null));
        verifyNoInteractions(traineeDao, usernameGenerator);
    }

    @Test
    void credentialsMatch_delegatesToAuthentication() {
        when(authenticationService.matchesTrainee(CREDENTIALS)).thenReturn(true);

        assertTrue(service.credentialsMatch(CREDENTIALS));
    }

    @Test
    void getProfile_returnsAuthenticatedTrainee() {
        when(authenticationService.authenticateTrainee(CREDENTIALS)).thenReturn(trainee);

        assertEquals(trainee, service.getProfile(CREDENTIALS));
    }

    @Test
    void getProfile_propagatesAuthenticationFailure() {
        when(authenticationService.authenticateTrainee(CREDENTIALS)).thenThrow(new AuthenticationFailedException());

        assertThrows(AuthenticationFailedException.class, () -> service.getProfile(CREDENTIALS));
    }

    @Test
    void changePassword_storesNewPassword() {
        when(authenticationService.authenticateTrainee(CREDENTIALS)).thenReturn(trainee);

        service.changePassword(CREDENTIALS, "newSecret");

        assertEquals("newSecret", trainee.getUser().getPassword());
    }

    @Test
    void changePassword_rejectsBlankPassword() {
        when(authenticationService.authenticateTrainee(CREDENTIALS)).thenReturn(trainee);

        assertThrows(InvalidRequestException.class, () -> service.changePassword(CREDENTIALS, " "));
        assertEquals("secret", trainee.getUser().getPassword());
    }

    @Test
    void update_changesProfileFieldsAndKeepsUsername() {
        when(authenticationService.authenticateTrainee(CREDENTIALS)).thenReturn(trainee);
        when(traineeDao.update(trainee)).thenReturn(trainee);

        Trainee updated = service.update(CREDENTIALS, "Johnny", "Smythe", LocalDate.of(1985, 2, 3), "New Street");

        assertEquals("Johnny", updated.getUser().getFirstName());
        assertEquals("Smythe", updated.getUser().getLastName());
        assertEquals(LocalDate.of(1985, 2, 3), updated.getDateOfBirth());
        assertEquals("New Street", updated.getAddress());
        assertEquals("John.Smith", updated.getUser().getUsername());
    }

    @Test
    void update_rejectsMissingLastName_withoutSaving() {
        when(authenticationService.authenticateTrainee(CREDENTIALS)).thenReturn(trainee);

        assertThrows(InvalidRequestException.class, () -> service.update(CREDENTIALS, "Johnny", "", null, null));
        verify(traineeDao, never()).update(any());
    }

    @Test
    void toggleActivation_flipsStateEachTime() {
        when(authenticationService.authenticateTrainee(CREDENTIALS)).thenReturn(trainee);

        assertFalse(service.toggleActivation(CREDENTIALS));
        assertTrue(service.toggleActivation(CREDENTIALS));
    }

    @Test
    void delete_removesAuthenticatedTrainee() {
        when(authenticationService.authenticateTrainee(CREDENTIALS)).thenReturn(trainee);

        service.delete(CREDENTIALS);

        verify(traineeDao).delete(trainee);
    }

    @Test
    void delete_doesNothing_whenAuthenticationFails() {
        when(authenticationService.authenticateTrainee(CREDENTIALS)).thenThrow(new AuthenticationFailedException());

        assertThrows(AuthenticationFailedException.class, () -> service.delete(CREDENTIALS));
        verify(traineeDao, never()).delete(any());
    }

    @Test
    void getUnassignedTrainers_returnsTrainersFromDao() {
        List<Trainer> trainers = List.of(trainerWithUsername("Anna.Lee"));
        when(authenticationService.authenticateTrainee(CREDENTIALS)).thenReturn(trainee);
        when(trainerDao.findNotAssignedToTrainee("John.Smith")).thenReturn(trainers);

        assertEquals(trainers, service.getUnassignedTrainers(CREDENTIALS));
    }

    @Test
    void updateTrainers_replacesAssignedTrainers() {
        Trainer anna = trainerWithUsername("Anna.Lee");
        Trainer mark = trainerWithUsername("Mark.Taylor");
        trainee.replaceTrainers(List.of(mark));
        when(authenticationService.authenticateTrainee(CREDENTIALS)).thenReturn(trainee);
        when(trainerDao.findAllByUsernames(any())).thenReturn(List.of(anna));
        when(traineeDao.update(trainee)).thenReturn(trainee);

        Trainee updated = service.updateTrainers(CREDENTIALS, List.of("Anna.Lee"));

        assertEquals(1, updated.getTrainers().size());
        assertTrue(updated.getTrainers().contains(anna));
    }

    @Test
    void updateTrainers_failsForUnknownTrainer_andKeepsCurrentTrainers() {
        Trainer mark = trainerWithUsername("Mark.Taylor");
        trainee.replaceTrainers(List.of(mark));
        when(authenticationService.authenticateTrainee(CREDENTIALS)).thenReturn(trainee);
        when(trainerDao.findAllByUsernames(any())).thenReturn(List.of());

        assertThrows(EntityNotFoundException.class, () -> service.updateTrainers(CREDENTIALS, List.of("Ghost.Trainer")));
        assertTrue(trainee.getTrainers().contains(mark));
        verify(traineeDao, never()).update(any());
    }
}
