package com.epam.gymcrm.dto.request;

/** The specialization is read only, so it is not part of an update. */
public record UpdateTrainerRequest(
        String firstName,
        String lastName,
        boolean active
) {}
