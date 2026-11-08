package com.epam.gymcrm.health;

import com.epam.gymcrm.dao.TrainingTypeDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

/** Trainers cannot be registered without training types, so an empty list means the service is down. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrainingTypesHealthIndicator extends AbstractHealthIndicator {

    private final TrainingTypeDao trainingTypeDao;

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        int count = trainingTypeDao.findAll().size();
        if (count == 0) {
            log.warn("Health check failed: no training types are loaded");
            builder.down().withDetail("reason", "No training types are loaded");
            return;
        }
        builder.up().withDetail("count", count);
    }
}
