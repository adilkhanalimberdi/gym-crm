package com.epam.gymcrm.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTraineeProfileRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Past @Schema(description = "ISO date, optional", example = "1992-07-20") LocalDate dateOfBirth,
        @Size(max = 255) @Schema(description = "Optional") String address,
        @JsonProperty("isActive") @NotNull Boolean isActive
) {}
