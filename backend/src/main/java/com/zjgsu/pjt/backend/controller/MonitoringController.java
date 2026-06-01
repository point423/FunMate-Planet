package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.monitoring.RequestMetrics;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class MonitoringController {

    private final RequestMetrics requestMetrics;

    public MonitoringController(RequestMetrics requestMetrics) {
        this.requestMetrics = requestMetrics;
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("http", requestMetrics.snapshot());
        return response;
    }
}
