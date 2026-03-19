package com.zjgsu.pjt.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/test/connection")
@CrossOrigin(origins = "*")
public class TestConnectionController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @GetMapping("/db")
    public Map<String, Object> testDatabase() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 测试数据库连接
            String dbVersion = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            String currentTime = jdbcTemplate.queryForObject("SELECT NOW()", String.class);

            result.put("success", true);
            result.put("message", "数据库连接成功");
            result.put("database_version", dbVersion);
            result.put("current_time", currentTime);
            result.put("datasource", "HikariDataSource");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "数据库连接失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/redis")
    public Map<String, Object> testRedis() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 测试 Redis 连接
            String testKey = "test:connection";
            String testValue = "Redis 连接测试 " + System.currentTimeMillis();
            
            // 写入数据
            redisTemplate.opsForValue().set(testKey, testValue, 10, TimeUnit.SECONDS);
            
            // 读取数据
            String value = (String) redisTemplate.opsForValue().get(testKey);
            
            // 检查 Redis 服务器信息（修复类型错误）
            Properties redisInfo = redisTemplate.getConnectionFactory()
                    .getConnection().serverCommands().info();
            
            result.put("success", true);
            result.put("message", "Redis 连接成功");
            result.put("test_write_read", value != null ? "成功" : "失败");
            result.put("redis_info", redisInfo != null ? "已获取 (" + redisInfo.size() + " 项)" : "未获取");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Redis 连接失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/all")
    public Map<String, Object> testAll() {
        Map<String, Object> result = new HashMap<>();

        // 测试数据库
        Map<String, Object> dbResult = testDatabase();
        result.put("database", dbResult);

        // 测试 Redis
        Map<String, Object> redisResult = testRedis();
        result.put("redis", redisResult);

        return result;
    }
}
