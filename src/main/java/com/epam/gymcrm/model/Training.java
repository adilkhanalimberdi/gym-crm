package com.epam.gymcrm.model;

import java.time.LocalDate;

public class Training {

    private final Long id;
    private final TrainingParticipant trainee;
    private final TrainingParticipant trainer;
    private final String trainingName;
    private final TrainingType trainingType;
    private final LocalDate trainingDate;
    private final int duration;

    /** A new training that has not been stored yet. */
    public Training(TrainingParticipant trainee,
                    TrainingParticipant trainer,
                    String trainingName,
                    TrainingType trainingType,
                    LocalDate trainingDate,
                    int duration) {
        this(null, trainee, trainer, trainingName, trainingType, trainingDate, duration);
    }

    public Training(Long id,
                    TrainingParticipant trainee,
                    TrainingParticipant trainer,
                    String trainingName,
                    TrainingType trainingType,
                    LocalDate trainingDate,
                    int duration) {
        this.id = id;
        this.trainee = trainee;
        this.trainer = trainer;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.duration = duration;
    }

    /** The same training with the id it got when it was stored. */
    public Training withId(Long newId) {
        return new Training(newId, trainee, trainer, trainingName, trainingType, trainingDate, duration);
    }

    public Long getId() {
        return id;
    }

    public TrainingParticipant getTrainee() {
        return trainee;
    }

    public TrainingParticipant getTrainer() {
        return trainer;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public TrainingType getTrainingType() {
        return trainingType;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    /** Duration in minutes. */
    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "Training[id=" + id + ", name=" + trainingName + ", type=" + trainingType.getName()
                + ", date=" + trainingDate + ", duration=" + duration + "]";
    }
}
