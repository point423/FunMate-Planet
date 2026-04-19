package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.service.DiscoverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discover")
@CrossOrigin(origins = "*")
public class DiscoverController {

    @Autowired
    private DiscoverService discoverService;

    @GetMapping("/nearby")
    public Result<List<User>> getNearby(@RequestParam(required = false) Double longitude,
                                        @RequestParam(required = false) Double latitude,
                                        @RequestParam(defaultValue = "10") Double radius) {
        try {
            if (longitude == null || latitude == null) {
                return Result.success(List.of());
            }
            List<User> users = discoverService.getNearbyUsers(longitude, latitude, radius);
            return Result.success(users);
        } catch (Exception e) {
            return Result.error(500, "搜索失败: " + e.getMessage());
        }
    }
// ... existing code ...


    @GetMapping("/ranking")
    public Result<List<User>> getRanking(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(discoverService.getRanking(limit));
    }

    @GetMapping("/random")
    public Result<User> getRandomUser() {
        User user = discoverService.getRandomUser();
        return user != null ? Result.success(user) : Result.error(404, "暂无用户");
    }

    @PostMapping("/location")
    public Result<String> updateLocation(@RequestBody LocationRequest locationRequest,
                                         @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        if (currentUserId == null) {
            return Result.error(401, "未授权");
        }
        try {
            discoverService.updateUserLocation(currentUserId, locationRequest.getLongitude(), locationRequest.getLatitude());
            return Result.success("位置更新成功");
        } catch (Exception e) {
            return Result.error(500, "位置更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/location")
    public Result<String> deleteLocation(@RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        if (currentUserId == null) {
            return Result.error(401, "未授权");
        }
        try {
            boolean success = discoverService.deleteUserLocation(currentUserId);
            return success ? Result.success("位置已删除") : Result.error(404, "位置信息不存在");
        } catch (Exception e) {
            return Result.error(500, "位置删除失败: " + e.getMessage());
        }
    }

    static class LocationRequest {
        private Double longitude;
        private Double latitude;

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }
    }
}
