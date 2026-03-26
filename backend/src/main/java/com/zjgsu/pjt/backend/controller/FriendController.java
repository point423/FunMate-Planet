package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "*")
public class FriendController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public Result<List<User>> getFriends() {
        return Result.success(userRepository.findAll());
    }

    @PostMapping("/requests")
    public Result<String> sendRequest(@RequestBody Map<String, Object> body) {
        return Result.success("好友申请已发送至用户 ID: " + body.get("targetUserId"));
    }

    @GetMapping("/requests")
    public Result<Map<String, Object>> getRequests() {
        Map<String, Object> data = new HashMap<>();
        data.put("incoming", new String[]{});
        data.put("outgoing", new String[]{});
        return Result.success(data);
    }

    @PostMapping("/requests/{id}/handle")
    public Result<String> handleRequest(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        String action = body.get("accept") ? "接受" : "拒绝";
        return Result.success("已" + action + " ID 为 " + id + " 的好友申请");
    }
}
