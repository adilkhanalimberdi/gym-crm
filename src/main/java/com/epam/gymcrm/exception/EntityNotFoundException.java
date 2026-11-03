package com.epam.gymcrm.exception;

import org.springframework.http.HttpStatus;

/** Thrown when an entity that a request refers to does not exist. */
public class EntityNotFoundException extends ApiException {

    public EntityNotFoundException(String entity, String identifier) {
        super(HttpStatus.NOT_FOUND, entity + " '" + identifier + "' was not found");
    }
}
