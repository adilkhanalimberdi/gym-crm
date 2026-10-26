package com.epam.gymcrm.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record TraineeProfileResponse(
        String username,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address,
        @JsonProperty("isActive") boolean isActive,
        List<TrainerSummary> trainers
) {}
