package com.epam.gymcrm.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TrainerProfileResponse(
        String username,
        String firstName,
        String lastName,
        TrainingTypeResponse specialization,
        @JsonProperty("isActive") boolean isActive,
        List<TraineeSummary> trainees
) {}
