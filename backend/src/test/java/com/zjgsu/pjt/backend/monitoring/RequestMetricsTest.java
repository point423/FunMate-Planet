package com.zjgsu.pjt.backend.monitoring;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RequestMetricsTest {

    @Test
    void snapshot_ReturnsRequestTimingAndErrorRate() {
        RequestMetrics metrics = new RequestMetrics();

        metrics.incrementActiveRequests();
        metrics.recordRequest(200, 120);
        metrics.recordRequest(500, 80);
        metrics.decrementActiveRequests();

        Map<String, Object> snapshot = metrics.snapshot();

        assertThat(snapshot).containsEntry("total_requests", 2L);
        assertThat(snapshot).containsEntry("error_requests", 1L);
        assertThat(snapshot).containsEntry("active_requests", 0L);
        assertThat(snapshot).containsEntry("max_response_time_ms", 120L);
        assertThat((Double) snapshot.get("error_rate")).isEqualTo(0.5);
        assertThat((Double) snapshot.get("average_response_time_ms")).isEqualTo(100.0);
    }
}
