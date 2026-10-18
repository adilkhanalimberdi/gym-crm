package com.epam.gymcrm.exception;

/** Thrown when an authenticated user tries to act on data that does not belong to them. */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
