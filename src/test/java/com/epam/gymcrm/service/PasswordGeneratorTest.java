package com.epam.gymcrm.service;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordGeneratorTest {

    private final PasswordGenerator generator = new PasswordGenerator();

    @Test
    void generate_returnsTenCharacterAlphanumericPassword() {
        String password = generator.generate();

        assertEquals(10, password.length());
        assertTrue(password.matches("[A-Za-z0-9]{10}"));
    }

    @Test
    void generate_returnsDifferentPasswordsOnRepeatedCalls() {
        long distinct = IntStream.range(0, 50).mapToObj(i -> generator.generate()).distinct().count();

        assertTrue(distinct > 1);
    }
}
