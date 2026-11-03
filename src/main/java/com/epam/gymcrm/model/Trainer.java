package com.epam.gymcrm.model;

import java.util.Objects;

public class Trainer {

    private final Long id;
    private final User user;
    private TrainingType specialization;

    /** A new trainer that has not been stored yet. */
    public Trainer(User user, TrainingType specialization) {
        this(null, user, specialization);
    }

    public Trainer(Long id, User user, TrainingType specialization) {
        this.id = id;
        this.user = user;
        this.specialization = specialization;
    }

    public TrainingParticipant toParticipant() {
        return new TrainingParticipant(id, user.getUsername(), user.getFirstName(), user.getLastName());
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public TrainingType getSpecialization() {
        return specialization;
    }

    public void setSpecialization(TrainingType specialization) {
        this.specialization = specialization;
    }

    /** Two trainers are the same when they have the same username, which is unique and never changes. */
    @Override
    public boolean equals(Object other) {
        return other instanceof Trainer trainer
                && Objects.equals(user.getUsername(), trainer.user.getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(user.getUsername());
    }

    @Override
    public String toString() {
        return "Trainer[id=" + id + ", username=" + user.getUsername() + ", specialization=" + specialization + "]";
    }
}
