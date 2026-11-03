package com.epam.gymcrm.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The domain works without Spring, Hibernate or a database. */
class DomainModelTest {

    private static final TrainingType YOGA = new TrainingType(1L, "Yoga");

    private static Trainer trainer(Long id, String username) {
        return new Trainer(id, new User("First", "Last", username, "pw"), YOGA);
    }

    private static Trainee trainee() {
        return new Trainee(new User("John", "Smith", "John.Smith", "pw"), null, null);
    }

    @Test
    void newUser_isActiveAndHasNoId() {
        User user = new User("John", "Smith", "John.Smith", "pw");

        assertTrue(user.isActive());
        assertNull(user.getId());
    }

    @Test
    void toggleActive_flipsTheState() {
        User user = new User("John", "Smith", "John.Smith", "pw");

        user.toggleActive();
        assertFalse(user.isActive());
        user.toggleActive();
        assertTrue(user.isActive());
    }

    @Test
    void userToString_leavesPasswordOut() {
        assertFalse(new User("John", "Smith", "John.Smith", "topSecret").toString().contains("topSecret"));
    }

    @Test
    void replaceTrainers_keepsOnlyTheGivenTrainers() {
        Trainee trainee = trainee();
        trainee.replaceTrainers(List.of(trainer(1L, "Anna.Lee")));

        trainee.replaceTrainers(List.of(trainer(2L, "Mark.Taylor")));

        assertEquals(Set.of(trainer(2L, "Mark.Taylor")), trainee.getTrainers());
    }

    @Test
    void getTrainers_cannotBeChangedFromOutside() {
        Set<Trainer> trainers = trainee().getTrainers();

        assertThrows(UnsupportedOperationException.class, () -> trainers.add(trainer(1L, "Anna.Lee")));
    }

    @Test
    void trainersAreEqual_whenTheirUsernamesAreEqual() {
        assertEquals(trainer(1L, "Anna.Lee"), trainer(1L, "Anna.Lee"));
        assertEquals(trainer(1L, "Anna.Lee").hashCode(), trainer(1L, "Anna.Lee").hashCode());
        assertNotEquals(trainer(1L, "Anna.Lee"), trainer(2L, "Mark.Taylor"));
    }

    @Test
    void toParticipant_namesTheProfile() {
        assertEquals(new TrainingParticipant(1L, "Anna.Lee", "First", "Last"), trainer(1L, "Anna.Lee").toParticipant());
        assertEquals(new TrainingParticipant(null, "John.Smith", "John", "Smith"), trainee().toParticipant());
    }

    @Test
    void withId_keepsEverythingElse() {
        TrainingParticipant participant = new TrainingParticipant(1L, "John.Smith", "John", "Smith");
        LocalDate date = LocalDate.of(2025, 3, 1);

        Training stored = new Training(participant, participant, "Morning Yoga", YOGA, date, 60).withId(7L);

        assertEquals(7L, stored.getId());
        assertEquals("Morning Yoga", stored.getTrainingName());
        assertEquals(date, stored.getTrainingDate());
        assertEquals(60, stored.getDuration());
    }
}
