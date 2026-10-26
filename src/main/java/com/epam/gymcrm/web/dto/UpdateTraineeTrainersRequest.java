package com.epam.gymcrm.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateTraineeTrainersRequest(@NotNull List<@NotNull @Valid TrainerUsername> trainers) {
}
