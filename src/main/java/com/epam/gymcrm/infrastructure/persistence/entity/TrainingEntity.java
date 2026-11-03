package com.epam.gymcrm.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "trainings")
public class TrainingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainee_id", nullable = false)
    private TraineeEntity trainee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private TrainerEntity trainer;

    @Column(name = "training_name", nullable = false)
    private String trainingName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingTypeEntity trainingType;

    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    /** Duration in minutes. */
    @Column(nullable = false)
    private int duration;

    protected TrainingEntity() {
        // required by JPA
    }

    public TrainingEntity(TraineeEntity trainee,
                          TrainerEntity trainer,
                          String trainingName,
                          TrainingTypeEntity trainingType,
                          LocalDate trainingDate,
                          int duration) {
        this.trainee = trainee;
        this.trainer = trainer;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.duration = duration;
    }

    public Long getId() {
        return id;
    }

    public TraineeEntity getTrainee() {
        return trainee;
    }

    public TrainerEntity getTrainer() {
        return trainer;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public TrainingTypeEntity getTrainingType() {
        return trainingType;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public int getDuration() {
        return duration;
    }
}
