package com.zjgsu.pjt.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestConnectionControllerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private RedisConnectionFactory connectionFactory;

    @Mock
    private RedisConnection redisConnection;

    @Mock
    private RedisServerCommands serverCommands;

    private TestConnectionController controller;

    @BeforeEach
    void setUp() {
        controller = new TestConnectionController();
        ReflectionTestUtils.setField(controller, "jdbcTemplate", jdbcTemplate);
        ReflectionTestUtils.setField(controller, "redisTemplate", redisTemplate);
    }

    @Test
    void testDatabase_ReturnsSuccessWhenQueriesWork() {
        when(jdbcTemplate.queryForObject("SELECT VERSION()", String.class)).thenReturn("8.0.36");
        when(jdbcTemplate.queryForObject("SELECT NOW()", String.class)).thenReturn("2026-06-16 12:00:00");

        Map<String, Object> result = controller.testDatabase();

        assertThat(result).containsEntry("success", true);
        assertThat(result).containsEntry("database_version", "8.0.36");
        assertThat(result).containsEntry("current_time", "2026-06-16 12:00:00");
    }

    @Test
    void testDatabase_ReturnsFailureWhenQueryThrows() {
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("db down"));

        Map<String, Object> result = controller.testDatabase();

        assertThat(result).containsEntry("success", false);
        assertThat(result.get("message")).asString().contains("db down");
    }

    @Test
    void testRedis_ReturnsSuccessWhenWriteReadAndInfoWork() {
        Properties properties = new Properties();
        properties.put("redis_version", "7.2");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), any(), eq(10L), eq(TimeUnit.SECONDS));
        when(valueOperations.get("test:connection")).thenReturn("ok");
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.serverCommands()).thenReturn(serverCommands);
        when(serverCommands.info()).thenReturn(properties);

        Map<String, Object> result = controller.testRedis();

        assertThat(result).containsEntry("success", true);
        assertThat(result).containsEntry("test_write_read", "成功");
        assertThat(result.get("redis_info")).asString().contains("1");
    }

    @Test
    void testRedis_ReturnsFailureWhenRedisThrows() {
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("redis down"));

        Map<String, Object> result = controller.testRedis();

        assertThat(result).containsEntry("success", false);
        assertThat(result.get("message")).asString().contains("redis down");
    }

    @Test
    void testAll_CombinesDatabaseAndRedisResults() {
        when(jdbcTemplate.queryForObject("SELECT VERSION()", String.class)).thenReturn("8.0.36");
        when(jdbcTemplate.queryForObject("SELECT NOW()", String.class)).thenReturn("2026-06-16 12:00:00");
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("redis down"));

        Map<String, Object> result = controller.testAll();

        assertThat(result).containsKeys("database", "redis");
        assertThat((Map<String, Object>) result.get("database")).containsEntry("success", true);
        assertThat((Map<String, Object>) result.get("redis")).containsEntry("success", false);
    }
}
