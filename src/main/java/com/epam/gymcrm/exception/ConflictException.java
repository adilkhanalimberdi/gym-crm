package com.epam.gymcrm.exception;

/** Thrown when a request cannot be applied to the current state of a resource. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
