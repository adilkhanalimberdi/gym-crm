package com.epam.gymcrm.dto.response;

import java.time.LocalDate;

public record TraineeTrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        TrainingTypeResponse trainingType,
        int trainingDuration,
        String trainerName
) {
}
