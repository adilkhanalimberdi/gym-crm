package com.epam.gymcrm.exception;

import org.springframework.http.HttpStatus;

/** Thrown when the username/password pair does not match an existing profile. */
public class AuthenticationFailedException extends ApiException {

    public AuthenticationFailedException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }
}
