package com.epam.gymcrm.dto.request;

import com.epam.gymcrm.model.TrainingType;

import java.time.LocalDateTime;

public record CreateTrainingRequest(
		Long traineeId,
		Long trainerId,
		String name,
		TrainingType type,
		LocalDateTime date,
		int durationMinutes
) {}
