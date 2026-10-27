package com.epam.gymcrm.dto;

import com.epam.gymcrm.exception.AccessDeniedException;
import org.springframework.lang.NonNull;

/** Username and password a user authenticates with. */
public record Credentials(String username, String password) {

    /** Guards endpoints that act on one user's own data: the caller has to be that user. */
    public Credentials requireOwner(String owner) {
        if (!username.equals(owner)) {
            throw new AccessDeniedException("Users can only access their own data");
        }
        return this;
    }

    /** The password is deliberately left out so it can never leak into logs. */
    @Override
    @NonNull
    public String toString() {
        return "Credentials[username=" + username + "]";
    }
}
