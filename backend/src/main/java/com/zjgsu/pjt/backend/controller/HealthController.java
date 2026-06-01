package com.zjgsu.pjt.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Value("${app.version:0.0.1-SNAPSHOT}")
    private String version;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "healthy");
        status.put("timestamp", Instant.now().toString());
        status.put("version", version);
        return status;
    }
}
