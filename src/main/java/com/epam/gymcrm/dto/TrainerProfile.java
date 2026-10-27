package com.epam.gymcrm.dto;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;

import java.util.List;

/** A trainer together with the trainees who have this trainer assigned. */
public record TrainerProfile(Trainer trainer, List<Trainee> trainees) {
}
