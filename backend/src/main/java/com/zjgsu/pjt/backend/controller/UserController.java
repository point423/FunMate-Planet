package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String GEO_KEY = "user:location";

    @GetMapping("/me")
    public Result<User> getMe(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        if (currentUserId == null) return Result.error(401, "未授权");

        return userRepository.findById(currentUserId)
                .map(Result::success)
                .orElse(Result.error(404, "用户不存在"));
    }

    @PutMapping("/me")
    public Result<User> updateMe(HttpServletRequest request, @RequestBody User profile) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        if (currentUserId == null) return Result.error(401, "未授权");

        User user = userRepository.findById(currentUserId).orElse(null);
        if (user == null) return Result.error(404, "用户不存在");

        if (profile.getNickname() != null) user.setNickname(profile.getNickname());
        if (profile.getAvatar() != null) user.setAvatar(profile.getAvatar());
        if (profile.getTags() != null) user.setTags(profile.getTags());
        if (profile.getAge() != null) user.setAge(profile.getAge());
        if (profile.getGender() != null) user.setGender(profile.getGender());

        return Result.success(userRepository.save(user));
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(Result::success)
                .orElse(Result.error(404, "用户不存在"));
    }

    @PostMapping("/location")
    public Result<String> updateLocation(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        if (currentUserId == null) return Result.error(401, "未授权");

        try {
            Double longitude = Double.valueOf(params.get("longitude").toString());
            Double latitude = Double.valueOf(params.get("latitude").toString());

            User user = userRepository.findById(currentUserId).orElse(null);
            if (user != null) {
                user.setLongitude(longitude);
                user.setLatitude(latitude);
                userRepository.save(user);
                stringRedisTemplate.opsForGeo().add(GEO_KEY, new Point(longitude, latitude), currentUserId.toString());
                return Result.success("位置上报成功");
            }
            return Result.error(404, "用户不存在");
        } catch (Exception e) {
            return Result.error(400, "参数解析失败");
        }
    }
}