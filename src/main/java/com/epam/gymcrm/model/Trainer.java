package com.epam.gymcrm.model;

import org.springframework.lang.NonNull;

public record Trainer(Long id,
                      String firstName,
                      String lastName,
                      String username,
                      String password,
                      boolean active,
                      TrainingType specialization) {

    public Trainer withId(Long newId) {
        return new Trainer(newId, firstName, lastName, username, password, active, specialization);
    }

    @Override
    @NonNull
    public String toString() {
        return "Trainer[id=" + id + ", username=" + username + ", specialization=" + specialization + "]";
    }
}
