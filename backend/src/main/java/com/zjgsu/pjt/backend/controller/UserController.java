package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<List<User>> getUsers() {
        return Result.success(userService.getAllUsers());
    }

    @GetMapping("/me")
    public Result<User> getMe(@RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        if (currentUserId == null) return Result.error(401, "未授权");

        User user = userService.findById(currentUserId);
        return user != null ? Result.success(user) : Result.error(404, "用户不存在");
    }

    @PutMapping("/me")
    public Result<User> updateMe(@RequestAttribute(value = "currentUserId", required = false) Long currentUserId,
                                 @RequestBody User profile) {
        if (currentUserId == null) return Result.error(401, "未授权");

        User updated = userService.updateProfile(currentUserId, profile);
        return updated != null ? Result.success(updated) : Result.error(404, "用户不存在");
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        return user != null ? Result.success(user) : Result.error(404, "用户不存在");
    }

    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        return updated != null ? Result.success(updated) : Result.error(404, "用户不存在");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        boolean success = userService.deleteUser(id);
        return success ? Result.success("用户删除成功") : Result.error(404, "用户不存在");
    }

    @PostMapping("/location")
    public Result<String> updateLocation(@RequestAttribute(value = "currentUserId", required = false) Long currentUserId,
                                         @RequestBody Map<String, Object> params) {
        if (currentUserId == null) return Result.error(401, "未授权");

        try {
            Double longitude = Double.valueOf(params.get("longitude").toString());
            Double latitude = Double.valueOf(params.get("latitude").toString());

            userService.updateLocation(currentUserId, longitude, latitude);
            return Result.success("位置上报成功");
        } catch (Exception e) {
            return Result.error(400, "参数解析失败");
        }
    }
}
