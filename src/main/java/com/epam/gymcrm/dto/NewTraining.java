package com.epam.gymcrm.dto;

import java.time.LocalDate;

/** Data needed to add a training; its type is the specialization of the trainer. */
public record NewTraining(String traineeUsername,
                          String trainerUsername,
                          String trainingName,
                          LocalDate trainingDate,
                          int duration) {
}
