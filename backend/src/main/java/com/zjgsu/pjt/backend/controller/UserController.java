package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.dto.UserProfileResponse;
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
    public Result<List<UserProfileResponse>> getUsers() {
        return Result.success(userService.getAllUsers());
    }

    @GetMapping("/me")
    public Result<UserProfileResponse> getMe(@RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        if (currentUserId == null) return Result.error(401, "Unauthorized");
        UserProfileResponse user = userService.findById(currentUserId);
        return user != null ? Result.success(user) : Result.error(404, "User not found");
    }

    @PutMapping("/me")
    public Result<UserProfileResponse> updateMe(@RequestAttribute(value = "currentUserId", required = false) Long currentUserId,
                                                @RequestBody User profile) {
        if (currentUserId == null) return Result.error(401, "Unauthorized");

        User updated = userService.updateProfile(currentUserId, profile);
        return updated != null ? Result.success(userService.findById(currentUserId)) : Result.error(404, "User not found");
    }

    @GetMapping("/{id:\\d+}")
    public Result<UserProfileResponse> getUserById(@PathVariable Long id) {
        UserProfileResponse user = userService.findById(id);
        return user != null ? Result.success(user) : Result.error(404, "User not found");
    }

    @GetMapping("/by-username")
    public Result<UserProfileResponse> searchByUsername(@RequestParam String username) {
        if (username == null || username.trim().isEmpty()) {
            return Result.error(400, "Username must not be empty");
        }

        return userService.findByUsername(username.trim())
                .map(user -> Result.success(userService.findById(user.getId())))
                .orElseGet(() -> Result.error(404, "User not found"));
    }

    @PutMapping("/{id:\\d+}")
    public Result<UserProfileResponse> updateUser(@PathVariable Long id,
                                                  @RequestBody User user,
                                                  @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        if (currentUserId == null) return Result.error(401, "Unauthorized");
        if (!id.equals(currentUserId)) {
            return Result.error(403, "No permission to update another user");
        }

        User updated = userService.updateUser(id, user);
        return updated != null ? Result.success(userService.findById(id)) : Result.error(404, "User not found");
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<String> deleteUser(@PathVariable Long id,
                                     @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        if (currentUserId == null) return Result.error(401, "Unauthorized");
        if (!id.equals(currentUserId)) {
            return Result.error(403, "No permission to delete another user");
        }

        boolean success = userService.deleteUser(id);
        return success ? Result.success("User deleted successfully") : Result.error(404, "User not found");
    }

    @PostMapping("/location")
    public Result<String> updateLocation(@RequestAttribute(value = "currentUserId", required = false) Long currentUserId,
                                         @RequestBody Map<String, Object> params) {
        if (currentUserId == null) return Result.error(401, "Unauthorized");

        try {
            Double longitude = Double.valueOf(params.get("longitude").toString());
            Double latitude = Double.valueOf(params.get("latitude").toString());
            userService.updateLocation(currentUserId, longitude, latitude);
            return Result.success("Location updated");
        } catch (Exception e) {
            return Result.error(400, "Failed to parse location parameters");
        }
    }
}
