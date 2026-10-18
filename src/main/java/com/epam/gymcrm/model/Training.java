package com.epam.gymcrm.model;

import java.time.LocalDateTime;

public record Training(Long id,
                       Long traineeId,
                       Long trainerId,
                       String trainingName,
                       TrainingType trainingType,
                       LocalDateTime trainingDate,
                       int durationMinutes) {

    public Training withId(Long newId) {
        return new Training(newId, traineeId, trainerId, trainingName, trainingType, trainingDate, durationMinutes);
    }
}
