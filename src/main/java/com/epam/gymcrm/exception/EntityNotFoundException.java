package com.epam.gymcrm.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entity, Long id) {
        super(entity + " with id " + id + " was not found");
    }
}
