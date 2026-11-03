package com.epam.gymcrm.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TrainerUsername(
        @NotBlank
        String username
) {
}
