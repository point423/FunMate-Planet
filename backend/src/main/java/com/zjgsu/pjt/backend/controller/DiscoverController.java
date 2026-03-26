package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
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
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/discover")
@CrossOrigin(origins = "*")
public class DiscoverController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserRepository userRepository;

    private static final String GEO_KEY = "user:location";

    @GetMapping("/nearby")
    public Result<List<User>> getNearby(@RequestParam Double longitude,
                                        @RequestParam Double latitude,
                                        @RequestParam(defaultValue = "10") Double radius) {
        try {
            Point point = new Point(longitude, latitude);
            Distance distance = new Distance(radius, RedisGeoCommands.DistanceUnit.KILOMETERS);
            Circle circle = new Circle(point, distance);

            var results = stringRedisTemplate.opsForGeo().radius(GEO_KEY, circle);

            if (results == null || results.getContent().isEmpty()) {
                return Result.success(new ArrayList<>());
            }

            List<Long> ids = results.getContent().stream()
                    .map(res -> Long.valueOf(res.getContent().getName()))
                    .collect(Collectors.toList());

            return Result.success(userRepository.findAllById(ids));
        } catch (Exception e) {
            return Result.error(500, "搜索失败: " + e.getMessage());
        }
    }

    @GetMapping("/ranking")
    public Result<List<User>> getRanking() {
        List<User> topUsers = userRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "averageScore"))
        ).getContent();
        return Result.success(topUsers);
    }

    // 补全 9.2 接口：随机匹配
    @GetMapping("/random")
    public Result<User> getRandomUser() {
        List<User> all = userRepository.findAll();
        if (all.isEmpty()) return Result.error(404, "暂无用户");
        return Result.success(all.get(new Random().nextInt(all.size())));
    }
}
