package com.epam.gymcrm.health;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

/** Reports the connection pool as down when requests are queueing for a connection and none is free. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionPoolHealthIndicator extends AbstractHealthIndicator {

    private final HikariDataSource dataSource;

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        HikariPoolMXBean pool = dataSource.getHikariPoolMXBean();
        if (pool == null) {
            builder.unknown().withDetail("reason", "The connection pool has not started yet");
            return;
        }

        int idle = pool.getIdleConnections();
        int waiting = pool.getThreadsAwaitingConnection();
        if (idle == 0 && waiting > 0) {
            log.warn("Health check failed: {} thread(s) are waiting for a database connection", waiting);
            builder.down();
        } else {
            builder.up();
        }
        builder.withDetail("active", pool.getActiveConnections())
                .withDetail("idle", idle)
                .withDetail("waiting", waiting)
                .withDetail("max", dataSource.getMaximumPoolSize());
    }
}
