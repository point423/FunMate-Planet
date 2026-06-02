package com.zjgsu.pjt.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HealthControllerTest {

    @Test
    void health_ReturnsStructuredStatus() {
        HealthController controller = new HealthController();
        ReflectionTestUtils.setField(controller, "version", "test-version");

        Map<String, Object> response = controller.health();

        assertThat(response).containsEntry("status", "healthy");
        assertThat(response).containsEntry("version", "test-version");
        assertThat(response.get("timestamp")).isInstanceOf(String.class);
    }

    @Test
    void health_WithHealthService_IncludesComponents() {
        HealthController controller = new HealthController();
        ReflectionTestUtils.setField(controller, "version", "test-version");

        // stub health service
        com.zjgsu.pjt.backend.monitoring.HealthService stub = new com.zjgsu.pjt.backend.monitoring.HealthService() {
            @Override
            public java.util.Map<String, Object> snapshot() {
                java.util.Map<String, Object> comps = new java.util.LinkedHashMap<>();
                java.util.Map<String, Object> db = new java.util.LinkedHashMap<>();
                db.put("status", "healthy");
                comps.put("database", db);
                java.util.Map<String, Object> redis = new java.util.LinkedHashMap<>();
                redis.put("status", "healthy");
                comps.put("redis", redis);
                comps.put("overall", "healthy");
                return comps;
            }
        };

        ReflectionTestUtils.setField(controller, "healthService", stub);

        java.util.Map<String, Object> response = controller.health();

        assertThat(response).containsKey("components");
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> comps = (java.util.Map<String, Object>) response.get("components");
        assertThat(comps).containsKey("database");
        assertThat(response).containsEntry("status", "healthy");
    }
}
