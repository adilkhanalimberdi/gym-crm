package com.epam.gymcrm.web.dto;

import java.time.LocalDate;

public record TraineeTrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        TrainingTypeResponse trainingType,
        int trainingDuration,
        String trainerName
) {}
