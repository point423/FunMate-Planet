package com.zjgsu.pjt.backend.monitoring;

import java.util.Map;

public interface HealthService {
    Map<String, Object> snapshot();
}
