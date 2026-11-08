package com.epam.gymcrm.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeapMemoryHealthIndicatorTest {

    private static final double MAX_USED_RATIO = 0.9;

    @Mock
    private MemoryMXBean memory;

    private Health healthWith(
            long used,
            long committed,
            long max
    ) {
        when(memory.getHeapMemoryUsage()).thenReturn(new MemoryUsage(
                0,
                used,
                committed,
                max
        ));
        return new HeapMemoryHealthIndicator(memory, MAX_USED_RATIO).health();
    }

    @Test
    void health_isUp_whenUsageIsAtTheLimit() {
        assertEquals(Status.UP, healthWith(90, 100, 100).getStatus());
    }

    @Test
    void health_isDown_whenUsageIsOverTheLimit() {
        assertEquals(Status.DOWN, healthWith(91, 100, 100).getStatus());
    }

    @Test
    void health_usesCommittedSize_whenHeapHasNoMaximum() {
        Health health = healthWith(50, 100, -1);

        assertEquals(Status.UP, health.getStatus());
        assertEquals(100L, health.getDetails().get("limitBytes"));
    }

    @Test
    void publicConstructor_readsTheRunningJvm() {
        assertEquals(Status.UP, new HeapMemoryHealthIndicator(1.0).health().getStatus());
    }
}
