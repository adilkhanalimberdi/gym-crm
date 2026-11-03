package com.epam.gymcrm.dto;

import java.time.LocalDate;

/** Optional filters for a trainer's trainings; a null or blank value means "do not filter". */
public record TrainerTrainingCriteria(LocalDate fromDate,
                                      LocalDate toDate,
                                      String traineeName) {

    public static TrainerTrainingCriteria none() {
        return new TrainerTrainingCriteria(null, null, null);
    }
}
