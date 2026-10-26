package com.epam.gymcrm.web.dto;

import jakarta.validation.constraints.NotBlank;

public record TrainerUsername(@NotBlank String username) {
}
