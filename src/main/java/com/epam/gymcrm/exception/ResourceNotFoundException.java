package com.epam.gymcrm.exception;

/** Thrown when a profile, a training type or another requested resource does not exist. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String entity, String identifier) {
        super(entity + " '" + identifier + "' was not found");
    }
}
