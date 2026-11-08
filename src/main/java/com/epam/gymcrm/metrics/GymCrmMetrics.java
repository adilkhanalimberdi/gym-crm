package com.epam.gymcrm.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Business metrics, published to Prometheus at /actuator/prometheus. Tags hold only bounded values
 * (never usernames), so the number of time series stays small.
 */
@Component
public class GymCrmMetrics {

    private static final String REGISTRATIONS = "gym.crm.registrations";
    private static final String AUTHENTICATIONS = "gym.crm.authentications";
    private static final String TRAININGS_CREATED = "gym.crm.trainings.created";

    private final MeterRegistry registry;
    private final Counter traineeRegistrations;
    private final Counter trainerRegistrations;
    private final Counter successfulAuthentications;
    private final Counter failedAuthentications;

    /** Fixed counters are created up front, so Prometheus sees them at 0 before the first event. */
    public GymCrmMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.traineeRegistrations = registry.counter(REGISTRATIONS, "role", "trainee");
        this.trainerRegistrations = registry.counter(REGISTRATIONS, "role", "trainer");
        this.successfulAuthentications = registry.counter(AUTHENTICATIONS, "result", "success");
        this.failedAuthentications = registry.counter(AUTHENTICATIONS, "result", "failure");
    }

    public void recordTraineeRegistration() {
        traineeRegistrations.increment();
    }

    public void recordTrainerRegistration() {
        trainerRegistrations.increment();
    }

    public void recordSuccessfulAuthentication() {
        successfulAuthentications.increment();
    }

    public void recordFailedAuthentication() {
        failedAuthentications.increment();
    }

    public void recordTrainingCreated(String trainingType) {
        registry.counter(TRAININGS_CREATED, "training_type", trainingType).increment();
    }
}
