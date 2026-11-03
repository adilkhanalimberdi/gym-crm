package com.epam.gymcrm.dto;

import java.time.LocalDate;

/** Optional filters for a trainee's trainings; a null or blank value means "do not filter". */
public record TraineeTrainingCriteria(LocalDate fromDate,
                                      LocalDate toDate,
                                      String trainerName,
                                      String trainingTypeName) {

    public static TraineeTrainingCriteria none() {
        return new TraineeTrainingCriteria(null, null, null, null);
    }
}
