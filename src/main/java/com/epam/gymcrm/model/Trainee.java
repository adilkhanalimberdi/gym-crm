package com.epam.gymcrm.model;

import java.time.LocalDate;

public record Trainee(Long id,
                      String firstName,
                      String lastName,
                      String username,
                      String password,
                      boolean active,
                      LocalDate dateOfBirth,
                      String address) {

    public Trainee withId(Long newId) {
        return new Trainee(newId, firstName, lastName, username, password, active, dateOfBirth, address);
    }

    /** Password is deliberately left out so it can never leak into logs. */
    @Override
    public String toString() {
        return "Trainee[id=" + id + ", username=" + username + ", active=" + active + "]";
    }
}
