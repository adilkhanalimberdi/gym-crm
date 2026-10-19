package com.epam.gymcrm.model;

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

    /** Password is deliberately left out so it can never leak into logs. */
    @Override
    public String toString() {
        return "Trainer[id=" + id + ", username=" + username + ", specialization=" + specialization + "]";
    }
}
