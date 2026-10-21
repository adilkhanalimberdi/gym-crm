package com.epam.gymcrm.storage;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Fills the storage beans with prepared data from a file right after they are created.
 * The file path comes from an external property file.
 */
public class StorageInitializer implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(StorageInitializer.class);
    private static final String TRAINEE = "TRAINEE";
    private static final String TRAINER = "TRAINER";
    private static final String TRAINING = "TRAINING";

    private final String seedFile;

    public StorageInitializer(String seedFile) {
        this.seedFile = seedFile;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (seedFile == null || seedFile.isBlank()) {
            return bean;
        }
        switch (beanName) {
            case InMemoryStorageConfig.TRAINEE_STORAGE -> load(TRAINEE, (Map<Long, Trainee>) bean, StorageInitializer::toTrainee);
            case InMemoryStorageConfig.TRAINER_STORAGE -> load(TRAINER, (Map<Long, Trainer>) bean, StorageInitializer::toTrainer);
            case InMemoryStorageConfig.TRAINING_STORAGE -> load(TRAINING, (Map<Long, Training>) bean, StorageInitializer::toTraining);
            default -> { /* not a storage bean */ }
        }
        return bean;
    }

    private <T> void load(String type, Map<Long, T> storage, java.util.function.Function<String[], Map.Entry<Long, T>> mapper) {
        List<String> lines = readLines();
        int count = 0;
        for (String line : lines) {
            String[] fields = line.split(",", -1);
            if (!type.equals(fields[0].trim())) {
                continue;
            }
            try {
                Map.Entry<Long, T> entry = mapper.apply(fields);
                storage.put(entry.getKey(), entry.getValue());
                count++;
            } catch (RuntimeException e) {
                throw new IllegalStateException("Invalid seed data line: " + line, e);
            }
        }
        log.info("Loaded {} {} record(s) from {}", count, type, seedFile);
    }

    private List<String> readLines() {
        Resource resource = new DefaultResourceLoader().getResource(seedFile);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read seed data file " + seedFile, e);
        }
    }

    private static Map.Entry<Long, Trainee> toTrainee(String[] f) {
        Long id = Long.valueOf(f[1].trim());
        return Map.entry(id, new Trainee(id, f[2].trim(), f[3].trim(), f[4].trim(), f[5].trim(),
                Boolean.parseBoolean(f[6].trim()), LocalDate.parse(f[7].trim()), f[8].trim()));
    }

    private static Map.Entry<Long, Trainer> toTrainer(String[] f) {
        Long id = Long.valueOf(f[1].trim());
        return Map.entry(id, new Trainer(id, f[2].trim(), f[3].trim(), f[4].trim(), f[5].trim(),
                Boolean.parseBoolean(f[6].trim()), TrainingType.valueOf(f[7].trim())));
    }

    private static Map.Entry<Long, Training> toTraining(String[] f) {
        Long id = Long.valueOf(f[1].trim());
        return Map.entry(id, new Training(id, Long.valueOf(f[2].trim()), Long.valueOf(f[3].trim()), f[4].trim(),
                TrainingType.valueOf(f[5].trim()), LocalDateTime.parse(f[6].trim()), Integer.parseInt(f[7].trim())));
    }
}
