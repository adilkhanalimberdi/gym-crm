package com.epam.gymcrm.dao;

import com.epam.gymcrm.dto.PageQuery;
import com.epam.gymcrm.dto.TraineeTrainingCriteria;
import com.epam.gymcrm.dto.TrainerTrainingCriteria;
import com.epam.gymcrm.model.Training;

import java.util.List;

public interface TrainingDao {

    Training save(Training training);

    List<Training> findByTrainee(String traineeUsername, TraineeTrainingCriteria criteria, PageQuery page);

    List<Training> findByTrainer(String trainerUsername, TrainerTrainingCriteria criteria, PageQuery page);
}
