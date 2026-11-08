package com.epam.gymcrm.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GymCrmMetricsTest {

    private final MeterRegistry registry = new SimpleMeterRegistry();
    private final GymCrmMetrics metrics = new GymCrmMetrics(registry);

    private double count(String name, String tag, String value) {
        return registry.get(name).tag(tag, value).counter().count();
    }

    @Test
    void countersStartAtZero() {
        assertEquals(0, count("gym.crm.registrations", "role", "trainee"));
        assertEquals(0, count("gym.crm.authentications", "result", "failure"));
    }

    @Test
    void registrationsAreCountedPerRole() {
        metrics.recordTraineeRegistration();
        metrics.recordTraineeRegistration();
        metrics.recordTrainerRegistration();

        assertEquals(2, count("gym.crm.registrations", "role", "trainee"));
        assertEquals(1, count("gym.crm.registrations", "role", "trainer"));
    }

    @Test
    void authenticationsAreCountedPerResult() {
        metrics.recordSuccessfulAuthentication();
        metrics.recordFailedAuthentication();
        metrics.recordFailedAuthentication();

        assertEquals(1, count("gym.crm.authentications", "result", "success"));
        assertEquals(2, count("gym.crm.authentications", "result", "failure"));
    }

    @Test
    void trainingsAreCountedPerType() {
        metrics.recordTrainingCreated("Yoga");
        metrics.recordTrainingCreated("Yoga");
        metrics.recordTrainingCreated("Zumba");

        assertEquals(2, count("gym.crm.trainings.created", "training_type", "Yoga"));
        assertEquals(1, count("gym.crm.trainings.created", "training_type", "Zumba"));
    }
}
