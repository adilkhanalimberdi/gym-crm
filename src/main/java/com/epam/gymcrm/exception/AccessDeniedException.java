package com.epam.gymcrm.exception;

import org.springframework.http.HttpStatus;

/** Thrown when an authenticated user tries to act on data that does not belong to them. */
public class AccessDeniedException extends ApiException {

    public AccessDeniedException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
