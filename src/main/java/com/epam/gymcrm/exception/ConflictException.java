package com.epam.gymcrm.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a request cannot be applied to the current state of a resource. */
public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
