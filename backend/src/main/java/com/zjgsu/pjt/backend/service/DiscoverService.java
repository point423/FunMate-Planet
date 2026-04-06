package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class DiscoverService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserRepository userRepository;

    private static final String GEO_KEY = "user:location";

    public List<User> getNearbyUsers(Double longitude, Double latitude, Double radius) {
        try {
            Point point = new Point(longitude, latitude);
            Distance distance = new Distance(radius, RedisGeoCommands.DistanceUnit.KILOMETERS);
            Circle circle = new Circle(point, distance);

            var results = stringRedisTemplate.opsForGeo().radius(GEO_KEY, circle);

            if (results == null || results.getContent().isEmpty()) {
                return new ArrayList<>();
            }

            List<Long> ids = results.getContent().stream()
                    .map(res -> Long.valueOf(res.getContent().getName()))
                    .collect(Collectors.toList());

            return userRepository.findAllById(ids);
        } catch (Exception e) {
            throw new RuntimeException("搜索失败: " + e.getMessage());
        }
    }

    public List<User> getRanking(int limit) {
        return userRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "averageScore"))
        ).getContent();
    }

    public User getRandomUser() {
        List<User> all = userRepository.findAll();
        if (all.isEmpty()) {
            return null;
        }
        return all.get(new Random().nextInt(all.size()));
    }

    public void updateUserLocation(Long userId, Double longitude, Double latitude) {
        try {
            stringRedisTemplate.opsForGeo().add(GEO_KEY,
                new Point(longitude, latitude),
                String.valueOf(userId));
        } catch (Exception e) {
            throw new RuntimeException("更新位置失败: " + e.getMessage());
        }
    }

    public boolean deleteUserLocation(Long userId) {
        try {
            Long removed = stringRedisTemplate.opsForGeo().remove(GEO_KEY, String.valueOf(userId));
            return removed != null && removed > 0;
        } catch (Exception e) {
            throw new RuntimeException("删除位置失败: " + e.getMessage());
        }
    }
}
