package com.epam.gymcrm.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AddTrainingRequest(
        @NotBlank String traineeUsername,
        @NotBlank String trainerUsername,
        @NotBlank @Size(max = 255) String trainingName,
        @NotNull @Schema(example = "2026-11-02") LocalDate trainingDate,
        @NotNull @Positive @Schema(description = "Minutes", example = "60") Integer duration
) {}
