package com.epam.gymcrm.exception;

/** Thrown when required input is missing or invalid. */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
