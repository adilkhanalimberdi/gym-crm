package com.epam.gymcrm.dto.request;

public record UpdateTrainerRequest(
        String firstName,
        String lastName,
        String specializationName
) {}
