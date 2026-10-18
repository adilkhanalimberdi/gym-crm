package com.epam.gymcrm.exception;

/** Thrown when the username/password pair does not match an existing profile. */
public class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException() {
        super("Invalid username or password");
    }
}
