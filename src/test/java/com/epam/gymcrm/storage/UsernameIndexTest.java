package com.epam.gymcrm.storage;

import com.epam.gymcrm.exception.DuplicateUsernameException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsernameIndexTest {

    private Map<Long, Trainee> trainees;
    private Map<Long, Trainer> trainers;
    private UsernameIndex index;

    @BeforeEach
    void setUp() {
        trainees = new ConcurrentHashMap<>();
        trainers = new ConcurrentHashMap<>();
        index = new UsernameIndex(trainees, trainers);
    }

    @Test
    void insertIfFree_runsInsert_whenUsernameFree() {
        assertEquals("saved", index.insertIfFree("John.Smith", () -> "saved"));
    }

    @Test
    void insertIfFree_throws_whenTakenByTrainee() {
        trainees.put(1L, new Trainee(1L, "John", "Smith", "John.Smith", "pw", true, LocalDate.of(1990, 1, 1), "Street"));

        assertThrows(DuplicateUsernameException.class, () -> index.insertIfFree("John.Smith", () -> "saved"));
    }

    @Test
    void insertIfFree_throws_whenTakenByTrainer() {
        trainers.put(1L, new Trainer(1L, "John", "Smith", "John.Smith", "pw", true, TrainingType.YOGA));

        assertThrows(DuplicateUsernameException.class, () -> index.insertIfFree("John.Smith", () -> "saved"));
    }

    @Test
    void insertIfFree_letsExactlyOneConcurrentCallerWin() throws Exception {
        int callers = 16;
        AtomicInteger inserted = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(callers);
        try {
            Future<?>[] results = new Future<?>[callers];
            for (int i = 0; i < callers; i++) {
                long id = i + 1;
                results[i] = pool.submit(() -> {
                    start.await();
                    try {
                        index.insertIfFree("John.Smith", () -> {
                            trainees.put(id, new Trainee(id, "John", "Smith", "John.Smith", "pw", true,
                                    LocalDate.of(1990, 1, 1), "Street"));
                            return inserted.incrementAndGet();
                        });
                    } catch (DuplicateUsernameException expected) {
                        // lost the race
                    }
                    return null;
                });
            }
            start.countDown();
            for (Future<?> result : results) {
                result.get(5, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        assertEquals(1, inserted.get());
        assertEquals(1, trainees.size());
    }
}
