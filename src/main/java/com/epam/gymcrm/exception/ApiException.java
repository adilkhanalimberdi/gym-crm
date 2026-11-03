package com.epam.gymcrm.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base of every domain exception. It carries the HTTP status the failure maps to, so the error handler
 * needs no code of its own for a new exception.
 */
@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
