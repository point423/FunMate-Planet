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
}
