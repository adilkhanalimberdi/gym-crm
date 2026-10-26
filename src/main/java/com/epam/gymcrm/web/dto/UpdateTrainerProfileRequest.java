package com.epam.gymcrm.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** The specialization is read only and therefore not part of the request. */
public record UpdateTrainerProfileRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @JsonProperty("isActive") @NotNull Boolean isActive
) {}
