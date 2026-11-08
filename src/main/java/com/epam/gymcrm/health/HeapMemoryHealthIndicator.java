package com.epam.gymcrm.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/** Reports the application as down when the used share of the heap goes over the configured limit. */
@Slf4j
@Component
public class HeapMemoryHealthIndicator extends AbstractHealthIndicator {

    private final MemoryMXBean memory;
    private final double maxUsedRatio;

    @Autowired
    public HeapMemoryHealthIndicator(@Value("${gym-crm.health.heap-max-used-ratio:0.9}") double maxUsedRatio) {
        this(ManagementFactory.getMemoryMXBean(), maxUsedRatio);
    }

    HeapMemoryHealthIndicator(MemoryMXBean memory, double maxUsedRatio) {
        this.memory = memory;
        this.maxUsedRatio = maxUsedRatio;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        MemoryUsage heap = memory.getHeapMemoryUsage();
        // getMax() is -1 when the JVM has no defined limit; the committed size is the best bound then
        long limit = heap.getMax() > 0 ? heap.getMax() : heap.getCommitted();
        double usedRatio = (double) heap.getUsed() / limit;
        if (usedRatio > maxUsedRatio) {
            log.warn("Health check failed: heap usage ratio {} is over the limit {}", usedRatio, maxUsedRatio);
            builder.down();
        } else {
            builder.up();
        }
        builder.withDetail("usedBytes", heap.getUsed())
                .withDetail("limitBytes", limit)
                .withDetail("usedRatio", usedRatio)
                .withDetail("maxUsedRatio", maxUsedRatio);
    }
}
