package com.epam.gymcrm.model;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Trainee {

    private final Long id;
    private final User user;
    private final Set<Trainer> trainers;
    private LocalDate dateOfBirth;
    private String address;

    /** A new trainee without trainers that has not been stored yet. */
    public Trainee(User user, LocalDate dateOfBirth, String address) {
        this(null, user, dateOfBirth, address, Set.of());
    }

    public Trainee(Long id,
                   User user,
                   LocalDate dateOfBirth,
                   String address,
                   Collection<Trainer> trainers) {
        this.id = id;
        this.user = user;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.trainers = new HashSet<>(trainers);
    }

    /** Replaces the assigned trainers with the given ones. */
    public void replaceTrainers(Collection<Trainer> newTrainers) {
        trainers.clear();
        trainers.addAll(newTrainers);
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /** The assigned trainers, read-only; use {@link #replaceTrainers} to change them. */
    public Set<Trainer> getTrainers() {
        return Collections.unmodifiableSet(trainers);
    }

    @Override
    public String toString() {
        return "Trainee[id=" + id + ", username=" + user.getUsername() + "]";
    }
}
