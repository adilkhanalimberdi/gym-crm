package com.epam.gymcrm.model;

/**
 * Who takes part in a training. A training only needs to name its trainee and trainer, so it holds this
 * instead of whole profiles with their passwords and trainer lists. The id is the trainee's or trainer's id.
 */
public record TrainingParticipant(Long id,
                                  String username,
                                  String firstName,
                                  String lastName) {
}
