package com.epam.gymcrm.storage;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** One in-memory storage (java.util.Map) per entity type, each its own Spring bean. */
@Configuration
public class InMemoryStorageConfig {

    public static final String TRAINEE_STORAGE = "traineeStorage";
    public static final String TRAINER_STORAGE = "trainerStorage";
    public static final String TRAINING_STORAGE = "trainingStorage";

    @Bean(TRAINEE_STORAGE)
    public Map<Long, Trainee> traineeStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean(TRAINER_STORAGE)
    public Map<Long, Trainer> trainerStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean(TRAINING_STORAGE)
    public Map<Long, Training> trainingStorage() {
        return new ConcurrentHashMap<>();
    }
}
