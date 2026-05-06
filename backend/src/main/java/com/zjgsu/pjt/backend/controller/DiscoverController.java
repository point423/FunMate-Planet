package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.service.DiscoverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discover")
@CrossOrigin(origins = "*")
public class DiscoverController {

    @Autowired
    private DiscoverService discoverService;

    /**
     * 附近的人搜索
     * 逻辑：下沉至 Service，Controller 仅负责路由
     */
    @GetMapping("/nearby")
    public Result<List<User>> getNearby(@RequestParam(required = false) Double longitude,
                                        @RequestParam(required = false) Double latitude,
                                        @RequestParam(defaultValue = "10") Double radius) {
        List<User> users = discoverService.getNearbyUsers(longitude, latitude, radius);
        return Result.success(users);
    }

    /**
     * 排行榜查询
     */
    @GetMapping("/ranking")
    public Result<List<User>> getRanking(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(discoverService.getRanking(limit));
    }

    /**
     * 随机匹配搭子
     */
    @GetMapping("/random")
    public Result<User> getRandomUser() {
        User user = discoverService.getRandomUser();
        return user != null ? Result.success(user) : Result.error(404, "暂无用户");
    }

    /**
     * 更新地理位置
     * 安全修复：使用 RequestAttribute 确保操作者身份
     */
    @PostMapping("/location")
    public Result<String> updateLocation(@RequestBody Map<String, Double> params,
                                         @RequestAttribute("currentUserId") Long currentUserId) {
        discoverService.updateUserLocation(currentUserId, params.get("longitude"), params.get("latitude"));
        return Result.success("位置更新成功");
    }

    /**
     * 删除地理位置 (CRUD - Delete)
     * 安全修复：防止水平越权 (IDOR)
     */
    @DeleteMapping("/location")
    public Result<String> deleteLocation(@RequestAttribute("currentUserId") Long currentUserId) {
        boolean success = discoverService.deleteUserLocation(currentUserId);
        return success ? Result.success("位置已删除") : Result.error(404, "位置信息不存在");
    }
}
