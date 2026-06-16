package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.monitoring.RequestMetrics;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MonitoringControllerTest {

    @Test
    void metrics_ReturnsTimestampAndHttpSnapshot() {
        RequestMetrics requestMetrics = new RequestMetrics();
        requestMetrics.recordRequest(200, 120);
        MonitoringController controller = new MonitoringController(requestMetrics);

        Map<String, Object> response = controller.metrics();

        assertThat(response).containsKeys("timestamp", "http");
        Map<?, ?> http = (Map<?, ?>) response.get("http");
        assertThat(http.get("total_requests")).isEqualTo(1L);
        assertThat(http.get("error_requests")).isEqualTo(0L);
        assertThat(http.get("max_response_time_ms")).isEqualTo(120L);
    }
}
