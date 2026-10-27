package com.epam.gymcrm.dto.request;

public record CreateTrainerRequest(
        String firstName,
        String lastName,
        Long specializationId
) {}
