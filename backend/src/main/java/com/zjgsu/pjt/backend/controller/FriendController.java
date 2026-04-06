package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.entity.FriendRequest;
import com.zjgsu.pjt.backend.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "*")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @GetMapping
    public Result<List<User>> getFriends(@RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        if (currentUserId == null) {
            return Result.error(401, "未授权");
        }
        return Result.success(friendService.getFriends(currentUserId));
    }

    @GetMapping("/{friendId}")
    public Result<User> getFriend(@PathVariable Long friendId) {
        User friend = friendService.getFriendById(friendId);
        return friend != null ? Result.success(friend) : Result.error(404, "好友不存在");
    }

    @DeleteMapping("/{friendId}")
    public Result<String> deleteFriend(@RequestAttribute(value = "currentUserId", required = false) Long currentUserId,
                                       @PathVariable Long friendId) {
        if (currentUserId == null) {
            return Result.error(401, "未授权");
        }
        boolean success = friendService.deleteFriend(currentUserId, friendId);
        return success ? Result.success("好友已删除") : Result.error(404, "好友关系不存在");
    }

    @PostMapping("/requests")
    public Result<String> sendRequest(@RequestAttribute(value = "currentUserId", required = false) Long currentUserId,
                                      @RequestBody Map<String, Object> body) {
        if (currentUserId == null) {
            return Result.error(401, "未授权");
        }

        Long targetUserId = Long.valueOf(body.get("targetUserId").toString());
        String msg = friendService.sendFriendRequest(currentUserId, targetUserId);
        return Result.created(msg);
    }

    @GetMapping("/requests")
    public Result<Map<String, Object>> getRequests(@RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        if (currentUserId == null) {
            return Result.error(401, "未授权");
        }

        return Result.success(friendService.getRequests(currentUserId));
    }

    @GetMapping("/requests/{id}")
    public Result<FriendRequest> getRequestById(@PathVariable Long id) {
        FriendRequest request = friendService.getRequestById(id);
        return request != null ? Result.success(request) : Result.error(404, "申请不存在");
    }

    @PostMapping("/requests/{id}/handle")
    public Result<String> handleRequest(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean success = friendService.handleRequest(id, body.get("accept"));
        return success ? Result.success("处理成功") : Result.error(404, "申请不存在");
    }

    @DeleteMapping("/requests/{id}")
    public Result<String> deleteRequest(@PathVariable Long id) {
        boolean success = friendService.deleteRequest(id);
        return success ? Result.success("申请记录已删除") : Result.error(404, "记录不存在");
    }
}
