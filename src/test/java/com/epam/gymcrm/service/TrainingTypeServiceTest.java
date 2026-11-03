package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.exception.AuthenticationFailedException;
import com.epam.gymcrm.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest {

    private static final Credentials CREDENTIALS = new Credentials("John.Smith", "secret");

    @Mock
    private TrainingTypeDao trainingTypeDao;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private TrainingType yoga;
    @InjectMocks
    private TrainingTypeService service;

    @Test
    void findAll_returnsTypesFromDao() {
        when(trainingTypeDao.findAll()).thenReturn(List.of(yoga));

        assertEquals(List.of(yoga), service.findAll(CREDENTIALS));
    }

    @Test
    void findAll_failsWithoutValidCredentials() {
        when(authenticationService.authenticate(CREDENTIALS)).thenThrow(new AuthenticationFailedException());

        assertThrows(AuthenticationFailedException.class, () -> service.findAll(CREDENTIALS));
        verifyNoInteractions(trainingTypeDao);
    }
}
