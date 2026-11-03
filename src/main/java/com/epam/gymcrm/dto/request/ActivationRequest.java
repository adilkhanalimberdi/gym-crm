package com.epam.gymcrm.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record ActivationRequest(
        @JsonProperty("isActive")
        @NotNull
        Boolean isActive
) {
}
