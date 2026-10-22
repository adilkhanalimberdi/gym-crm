package com.epam.gymcrm.service;

import com.epam.gymcrm.exception.InvalidRequestException;

/** Required-field checks run before any create/update action. */
final class Validation {

    private Validation() {
    }

    static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidRequestException(fieldName + " is required");
        }
        return value.trim();
    }

    static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new InvalidRequestException(fieldName + " is required");
        }
        return value;
    }

    static int requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new InvalidRequestException(fieldName + " must be a positive number");
        }
        return value;
    }
}
