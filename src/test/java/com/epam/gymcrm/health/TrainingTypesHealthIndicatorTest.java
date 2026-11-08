package com.epam.gymcrm.health;

import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypesHealthIndicatorTest {

    @Mock
    private TrainingTypeDao trainingTypeDao;
    @InjectMocks
    private TrainingTypesHealthIndicator indicator;

    @Test
    void health_isUp_whenTrainingTypesAreLoaded() {
        when(trainingTypeDao.findAll()).thenReturn(List.of(mock(TrainingType.class)));

        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(1, health.getDetails().get("count"));
    }

    @Test
    void health_isDown_whenNoTrainingTypesAreLoaded() {
        when(trainingTypeDao.findAll()).thenReturn(List.of());

        assertEquals(Status.DOWN, indicator.health().getStatus());
    }

    @Test
    void health_isDown_whenDatabaseFails() {
        when(trainingTypeDao.findAll()).thenThrow(new IllegalStateException("database is unavailable"));

        assertEquals(Status.DOWN, indicator.health().getStatus());
    }
}
