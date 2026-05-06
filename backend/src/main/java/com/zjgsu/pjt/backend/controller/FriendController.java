package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.entity.FriendRequest;
import com.zjgsu.pjt.backend.service.FriendService;
import jakarta.servlet.http.HttpServletRequest;
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
    public Result<List<User>> getFriends(@RequestAttribute("currentUserId") Long currentUserId) {
        return Result.success(friendService.getFriends(currentUserId));
    }

    @GetMapping("/{friendId}")
    public Result<User> getFriend(@PathVariable Long friendId) {
        User friend = friendService.getFriendById(friendId);
        return friend != null ? Result.success(friend) : Result.error(404, "好友不存在");
    }

    @DeleteMapping("/{friendId}")
    public Result<String> deleteFriend(@RequestAttribute("currentUserId") Long currentUserId,
                                       @PathVariable Long friendId) {
        boolean success = friendService.deleteFriend(currentUserId, friendId);
        return success ? Result.success("好友已删除") : Result.error(404, "好友关系不存在");
    }

    @PostMapping("/requests")
    public Result<String> sendRequest(@RequestAttribute("currentUserId") Long currentUserId,
                                      @RequestBody Map<String, Object> body) {
        Long targetUserId = Long.valueOf(body.get("targetUserId").toString());
        String msg = friendService.sendFriendRequest(currentUserId, targetUserId);
        return Result.created(msg);
    }

    @GetMapping("/requests")
    public Result<Map<String, Object>> getRequests(@RequestAttribute("currentUserId") Long currentUserId) {
        return Result.success(friendService.getRequests(currentUserId));
    }

    @GetMapping("/requests/{id}")
    public Result<FriendRequest> getRequestById(@PathVariable Long id, @RequestAttribute("currentUserId") Long currentUserId) {
        FriendRequest request = friendService.getRequestById(id, currentUserId);
        return request != null ? Result.success(request) : Result.error(403, "无权查看或申请不存在");
    }

    @PostMapping("/requests/{id}/handle")
    public Result<String> handleRequest(@PathVariable Long id, 
                                       @RequestBody Map<String, Boolean> body,
                                       @RequestAttribute("currentUserId") Long currentUserId) {
        boolean success = friendService.handleRequest(id, body.get("accept"), currentUserId);
        return success ? Result.success("处理成功") : Result.error(403, "无权处理该申请或申请不存在");
    }

    @DeleteMapping("/requests/{id}")
    public Result<String> deleteRequest(@PathVariable Long id, @RequestAttribute("currentUserId") Long currentUserId) {
        boolean success = friendService.deleteRequest(id, currentUserId);
        return success ? Result.success("申请记录已删除") : Result.error(403, "无权删除或记录不存在");
    }
}
