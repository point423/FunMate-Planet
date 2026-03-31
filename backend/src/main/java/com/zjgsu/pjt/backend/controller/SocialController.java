package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.Friendship;
import com.zjgsu.pjt.backend.repository.FriendshipRepository;
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
    private FriendshipRepository friendshipRepository;

    @PostMapping("/{id}/follow")
    public Result<String> follow(@PathVariable Long id,
                                 @RequestBody Map<String, Boolean> body,
                                 HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        if (currentUserId == null) return Result.error(401, "未授权");

        boolean follow = Boolean.TRUE.equals(body.get("follow"));
        Friendship existing = friendshipRepository.findByUserIdAndFriendId(currentUserId, id);

        if (follow) {
            if (existing == null) {
                Friendship f = new Friendship();
                f.setUserId(currentUserId);
                f.setFriendId(id);
                friendshipRepository.save(f);
            }
            return Result.success("关注成功");
        } else {
            if (existing != null) {
                friendshipRepository.delete(existing);
            }
            return Result.success("已取消关注");
        }
    }

    @GetMapping("/{id}/followers")
    public Result<Page<Friendship>> getFollowers(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "1") int pageNum) {
        return Result.success(friendshipRepository.findByFriendId(id, PageRequest.of(Math.max(pageNum - 1, 0), 10)));
    }

    @GetMapping("/{id}/following")
    public Result<Page<Friendship>> getFollowing(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "1") int pageNum) {
        return Result.success(friendshipRepository.findByUserId(id, PageRequest.of(Math.max(pageNum - 1, 0), 10)));
    }
}