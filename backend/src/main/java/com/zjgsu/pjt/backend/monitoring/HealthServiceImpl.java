package com.zjgsu.pjt.backend.monitoring;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class HealthServiceImpl implements HealthService {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    public HealthServiceImpl(DataSource dataSource, RedisConnectionFactory redisConnectionFactory) {
        this.dataSource = dataSource;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public Map<String, Object> snapshot() {
        Map<String, Object> components = new LinkedHashMap<>();
        boolean healthy = true;

        // Database check
        Map<String, Object> db = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                db.put("status", "healthy");
            } else {
                db.put("status", "unhealthy");
                healthy = false;
            }
        } catch (Exception e) {
            db.put("status", "unhealthy");
            db.put("error", e.getMessage());
            healthy = false;
        }
        components.put("database", db);

        // Redis check
        Map<String, Object> redis = new LinkedHashMap<>();
        try (RedisConnection conn = redisConnectionFactory.getConnection()) {
            String pong = conn.ping();
            if (pong != null && pong.equalsIgnoreCase("PONG")) {
                redis.put("status", "healthy");
            } else {
                redis.put("status", "unhealthy");
                healthy = false;
            }
        } catch (Exception e) {
            redis.put("status", "unhealthy");
            redis.put("error", e.getMessage());
            healthy = false;
        }
        components.put("redis", redis);

        components.put("overall", healthy ? "healthy" : "unhealthy");
        return components;
    }
}
