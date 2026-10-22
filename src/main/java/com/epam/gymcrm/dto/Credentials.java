package com.epam.gymcrm.dto;

import org.springframework.lang.NonNull;

/** Username and password a user authenticates with. */
public record Credentials(String username, String password) {

    /** The password is deliberately left out so it can never leak into logs. */
    @Override
    @NonNull
    public String toString() {
        return "Credentials[username=" + username + "]";
    }
}
