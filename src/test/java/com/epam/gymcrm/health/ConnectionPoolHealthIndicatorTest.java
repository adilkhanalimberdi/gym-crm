package com.epam.gymcrm.health;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectionPoolHealthIndicatorTest {

    @Mock
    private HikariDataSource dataSource;
    @Mock
    private HikariPoolMXBean pool;
    @InjectMocks
    private ConnectionPoolHealthIndicator indicator;

    @Test
    void health_isUp_whenConnectionIsFree() {
        when(dataSource.getHikariPoolMXBean()).thenReturn(pool);
        when(pool.getIdleConnections()).thenReturn(2);
        when(pool.getActiveConnections()).thenReturn(3);
        when(dataSource.getMaximumPoolSize()).thenReturn(5);

        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(3, health.getDetails().get("active"));
        assertEquals(5, health.getDetails().get("max"));
    }

    @Test
    void health_isUp_whenPoolIsBusyButNobodyWaits() {
        when(dataSource.getHikariPoolMXBean()).thenReturn(pool);

        assertEquals(Status.UP, indicator.health().getStatus());
    }

    @Test
    void health_isDown_whenThreadsWaitAndNoConnectionIsFree() {
        when(dataSource.getHikariPoolMXBean()).thenReturn(pool);
        when(pool.getThreadsAwaitingConnection()).thenReturn(1);

        assertEquals(Status.DOWN, indicator.health().getStatus());
    }

    @Test
    void health_isUnknown_whenPoolHasNotStarted() {
        when(dataSource.getHikariPoolMXBean()).thenReturn(null);

        assertEquals(Status.UNKNOWN, indicator.health().getStatus());
    }
}
