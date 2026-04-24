package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.Friendship;
import com.zjgsu.pjt.backend.service.FriendshipService;
import jakarta.servlet.http.HttpServletRequest;
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

    @PostMapping("/{id}/follow")
    public Result<String> follow(@PathVariable Long id,
                                 @RequestBody Map<String, Boolean> body,
                                 HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        if (currentUserId == null) return Result.error(401, "未授权");

        boolean follow = Boolean.TRUE.equals(body.get("follow"));
        String msg = friendshipService.follow(currentUserId, id, follow);
        return Result.success(msg);
    }

    @GetMapping("/{id}/followers")
    public Result<Page<Friendship>> getFollowers(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "1") int pageNum) {
        return Result.success(friendshipService.getFollowers(id, PageRequest.of(Math.max(pageNum - 1, 0), 10)));
    }

    @GetMapping("/{id}/following")
    public Result<Page<Friendship>> getFollowing(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "1") int pageNum) {
        return Result.success(friendshipService.getFollowing(id, PageRequest.of(Math.max(pageNum - 1, 0), 10)));
    }

    /**
     * 补全：删除社交关系接口
     */
    @DeleteMapping("/friendship/{id}")
    public Result<String> deleteFriendship(@PathVariable Long id) {
        boolean success = friendshipService.deleteFriendship(id);
        return success ? Result.success("关系记录已物理删除") : Result.error(404, "记录不存在");
    }
}
