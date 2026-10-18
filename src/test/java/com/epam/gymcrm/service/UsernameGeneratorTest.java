package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.UserDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {

    @Mock
    private UserDao userDao;
    @InjectMocks
    private UsernameGenerator generator;

    @Test
    void generate_joinsFirstAndLastNameWithDot_whenFree() {
        when(userDao.findUsernamesStartingWith("John.Smith")).thenReturn(List.of());

        assertEquals("John.Smith", generator.generate("John", "Smith"));
    }

    @Test
    void generate_trimsNames() {
        when(userDao.findUsernamesStartingWith("John.Smith")).thenReturn(List.of());

        assertEquals("John.Smith", generator.generate("  John ", " Smith  "));
    }

    @Test
    void generate_appendsSerialOne_whenBaseIsTaken() {
        when(userDao.findUsernamesStartingWith("John.Smith")).thenReturn(List.of("John.Smith"));

        assertEquals("John.Smith1", generator.generate("John", "Smith"));
    }

    @Test
    void generate_skipsTakenSerials() {
        when(userDao.findUsernamesStartingWith("John.Smith"))
                .thenReturn(List.of("John.Smith", "John.Smith1", "John.Smith2"));

        assertEquals("John.Smith3", generator.generate("John", "Smith"));
    }

    @Test
    void generate_ignoresLongerNamesThatOnlyShareThePrefix() {
        when(userDao.findUsernamesStartingWith("John.Smith")).thenReturn(List.of("John.Smithson"));

        assertEquals("John.Smith", generator.generate("John", "Smith"));
    }
}
