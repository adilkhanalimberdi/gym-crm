package com.epam.gymcrm.dto;

/** Username and password a user authenticates with. */
public record Credentials(String username, String password) {

    /** The password is deliberately left out so it can never leak into logs. */
    @Override
    public String toString() {
        return "Credentials[username=" + username + "]";
    }
}
