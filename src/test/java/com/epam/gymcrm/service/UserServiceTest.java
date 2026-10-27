package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.exception.AuthenticationFailedException;
import com.epam.gymcrm.exception.ConflictException;
import com.epam.gymcrm.exception.InvalidRequestException;
import com.epam.gymcrm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Credentials CREDENTIALS = new Credentials("John.Smith", "secret");

    @Mock
    private AuthenticationService authenticationService;
    @InjectMocks
    private UserService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("John", "Smith", "John.Smith", "secret");
    }

    @Test
    void changePassword_storesNewPassword() {
        when(authenticationService.authenticate(CREDENTIALS)).thenReturn(user);

        service.changePassword(CREDENTIALS, "newSecret");

        assertEquals("newSecret", user.getPassword());
    }

    @Test
    void changePassword_rejectsBlankPassword() {
        when(authenticationService.authenticate(CREDENTIALS)).thenReturn(user);

        assertThrows(InvalidRequestException.class, () -> service.changePassword(CREDENTIALS, " "));
        assertEquals("secret", user.getPassword());
    }

    @Test
    void changePassword_propagatesAuthenticationFailure() {
        when(authenticationService.authenticate(CREDENTIALS)).thenThrow(new AuthenticationFailedException());

        assertThrows(AuthenticationFailedException.class, () -> service.changePassword(CREDENTIALS, "newSecret"));
    }

    @Test
    void changeActivation_deactivatesActiveUser() {
        when(authenticationService.authenticate(CREDENTIALS)).thenReturn(user);

        service.changeActivation(CREDENTIALS, false);

        assertFalse(user.isActive());
    }

    @Test
    void changeActivation_activatesInactiveUser() {
        user.setActive(false);
        when(authenticationService.authenticate(CREDENTIALS)).thenReturn(user);

        service.changeActivation(CREDENTIALS, true);

        assertTrue(user.isActive());
    }

    @Test
    void changeActivation_isNotIdempotent_repeatingTheSameStateIsAConflict() {
        when(authenticationService.authenticate(CREDENTIALS)).thenReturn(user);

        assertThrows(ConflictException.class, () -> service.changeActivation(CREDENTIALS, true));
        assertTrue(user.isActive());
    }
}
