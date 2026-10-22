package com.epam.gymcrm.dto;

import java.time.LocalDate;

/** Data needed to add a training. */
public record NewTraining(String traineeUsername,
                          String trainerUsername,
                          String trainingName,
                          String trainingTypeName,
                          LocalDate trainingDate,
                          int duration) {
}
