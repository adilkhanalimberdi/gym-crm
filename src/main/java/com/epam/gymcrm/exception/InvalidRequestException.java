package com.epam.gymcrm.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a request is well-formed but its values do not make sense together. */
public class InvalidRequestException extends ApiException {

    public InvalidRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
