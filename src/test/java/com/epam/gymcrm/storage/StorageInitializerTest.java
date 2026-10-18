package com.epam.gymcrm.storage;

import com.epam.gymcrm.config.StorageConfig;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageInitializerTest {

    private static final String DATA = """
            # comment
            TRAINEE,1,John,Smith,John.Smith,pw1,true,1990-05-14,12 Main Street

            TRAINER,2,Anna,Lee,Anna.Lee,pw2,false,YOGA
            TRAINING,3,1,2,Morning Yoga,YOGA,2025-03-01T09:00,60
            """;

    @TempDir
    Path tempDir;

    private StorageInitializer initializerFor(String content) throws IOException {
        Path file = tempDir.resolve("seed.txt");
        Files.writeString(file, content);
        return new StorageInitializer(file.toUri().toString());
    }

    @Test
    void loadsTraineesIntoTraineeStorageOnly() throws IOException {
        Map<Long, Trainee> storage = new HashMap<>();

        Object result = initializerFor(DATA).postProcessAfterInitialization(storage, StorageConfig.TRAINEE_STORAGE);

        assertSame(storage, result);
        assertEquals(1, storage.size());
        assertEquals("John.Smith", storage.get(1L).username());
        assertEquals("12 Main Street", storage.get(1L).address());
    }

    @Test
    void loadsTrainersIntoTrainerStorage() throws IOException {
        Map<Long, Trainer> storage = new HashMap<>();

        initializerFor(DATA).postProcessAfterInitialization(storage, StorageConfig.TRAINER_STORAGE);

        assertEquals(TrainingType.YOGA, storage.get(2L).specialization());
        assertEquals(false, storage.get(2L).active());
    }

    @Test
    void loadsTrainingsIntoTrainingStorage() throws IOException {
        Map<Long, Training> storage = new HashMap<>();

        initializerFor(DATA).postProcessAfterInitialization(storage, StorageConfig.TRAINING_STORAGE);

        assertEquals(60, storage.get(3L).durationMinutes());
        assertEquals("Morning Yoga", storage.get(3L).trainingName());
    }

    @Test
    void leavesOtherBeansUntouched() throws IOException {
        Map<Long, Trainee> other = new HashMap<>();

        initializerFor(DATA).postProcessAfterInitialization(other, "someOtherBean");

        assertTrue(other.isEmpty());
    }

    @Test
    void skipsLoading_whenNoFileConfigured() {
        Map<Long, Trainee> storage = new HashMap<>();

        new StorageInitializer("").postProcessAfterInitialization(storage, StorageConfig.TRAINEE_STORAGE);
        new StorageInitializer(null).postProcessAfterInitialization(storage, StorageConfig.TRAINEE_STORAGE);

        assertTrue(storage.isEmpty());
    }

    @Test
    void failsFast_onMalformedLine() throws IOException {
        StorageInitializer initializer = initializerFor("TRAINEE,not-a-number,John\n");

        assertThrows(IllegalStateException.class,
                () -> initializer.postProcessAfterInitialization(new HashMap<Long, Trainee>(), StorageConfig.TRAINEE_STORAGE));
    }

    @Test
    void failsFast_whenFileMissing() {
        StorageInitializer initializer = new StorageInitializer(tempDir.resolve("missing.txt").toUri().toString());

        assertThrows(UncheckedIOException.class,
                () -> initializer.postProcessAfterInitialization(new HashMap<Long, Trainee>(), StorageConfig.TRAINEE_STORAGE));
    }
}
