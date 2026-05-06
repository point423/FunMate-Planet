package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.Friendship;
import com.zjgsu.pjt.backend.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class SocialController {

    @Autowired
    private FriendshipService friendshipService;

    /**
     * 关注/取消关注用户
     * 逻辑：Controller 仅负责参数提取，逻辑全下沉
     */
    @PostMapping("/{id}/follow")
    public Result<String> follow(@PathVariable Long id,
                                 @RequestBody Map<String, Boolean> body,
                                 @RequestAttribute("currentUserId") Long currentUserId) {
        boolean follow = Boolean.TRUE.equals(body.get("follow"));
        String msg = friendshipService.follow(currentUserId, id, follow);
        return Result.success(msg);
    }

    /**
     * 获取粉丝列表
     */
    @GetMapping("/{id}/followers")
    public Result<Page<Friendship>> getFollowers(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "1") int pageNum) {
        return Result.success(friendshipService.getFollowers(id, PageRequest.of(Math.max(pageNum - 1, 0), 10)));
    }

    /**
     * 获取关注列表
     */
    @GetMapping("/{id}/following")
    public Result<Page<Friendship>> getFollowing(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "1") int pageNum) {
        return Result.success(friendshipService.getFollowing(id, PageRequest.of(Math.max(pageNum - 1, 0), 10)));
    }

    /**
     * 物理删除关系记录
     * 安全修复：增加越权校验，只有关系人可以删除（或者简化为功能补充）
     */
    @DeleteMapping("/friendship/{id}")
    public Result<String> deleteFriendship(@PathVariable Long id,
                                           @RequestAttribute("currentUserId") Long currentUserId) {
        boolean success = friendshipService.deleteFriendship(id, currentUserId);
        return success ? Result.success("关系记录已物理删除") : Result.error(403, "无权删除或记录不存在");
    }
}
