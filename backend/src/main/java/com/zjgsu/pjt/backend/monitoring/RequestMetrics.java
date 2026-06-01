package com.zjgsu.pjt.backend.monitoring;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RequestMetrics {

    private final AtomicLong totalRequests = new AtomicLong();
    private final AtomicLong errorRequests = new AtomicLong();
    private final AtomicLong activeRequests = new AtomicLong();
    private final AtomicLong totalResponseTimeMs = new AtomicLong();
    private final AtomicLong maxResponseTimeMs = new AtomicLong();

    public void incrementActiveRequests() {
        activeRequests.incrementAndGet();
    }

    public void decrementActiveRequests() {
        activeRequests.updateAndGet(value -> Math.max(0, value - 1));
    }

    public void recordRequest(int status, long responseTimeMs) {
        totalRequests.incrementAndGet();
        totalResponseTimeMs.addAndGet(responseTimeMs);
        maxResponseTimeMs.accumulateAndGet(responseTimeMs, Math::max);
        if (status >= 500) {
            errorRequests.incrementAndGet();
        }
    }

    public Map<String, Object> snapshot() {
        long total = totalRequests.get();
        long errors = errorRequests.get();
        long totalTime = totalResponseTimeMs.get();

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("total_requests", total);
        metrics.put("error_requests", errors);
        metrics.put("error_rate", total == 0 ? 0.0 : errors * 1.0 / total);
        metrics.put("active_requests", activeRequests.get());
        metrics.put("average_response_time_ms", total == 0 ? 0.0 : totalTime * 1.0 / total);
        metrics.put("max_response_time_ms", maxResponseTimeMs.get());
        return metrics;
    }
}
