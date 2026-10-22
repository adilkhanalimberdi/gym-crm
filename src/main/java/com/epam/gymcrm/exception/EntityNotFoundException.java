package com.epam.gymcrm.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entity, String identifier) {
        super(entity + " '" + identifier + "' was not found");
    }
}
