package com.epam.gymcrm.dto;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Passwords must never show up when objects are logged. */
class CredentialsTest {

    @Test
    void credentialsToString_doesNotContainPassword() {
        String text = new Credentials("John.Smith", "topSecret1").toString();

        assertTrue(text.contains("John.Smith"));
        assertFalse(text.contains("topSecret1"));
    }

    @Test
    void userAndTraineeToString_doNotContainPassword() {
        User user = new User("John", "Smith", "John.Smith", "topSecret1");

        assertFalse(user.toString().contains("topSecret1"));
        assertFalse(new Trainee(user, null, null).toString().contains("topSecret1"));
    }
}
