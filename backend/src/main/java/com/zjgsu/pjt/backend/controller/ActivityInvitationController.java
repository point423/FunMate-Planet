package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.ActivityInvitation;
import com.zjgsu.pjt.backend.service.ActivityInvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/activity-invitations")
@CrossOrigin(origins = "*")
public class ActivityInvitationController {

    @Autowired
    private ActivityInvitationService activityInvitationService;

    @GetMapping
    public Result<Map<String, Object>> getMyInvitations(@RequestAttribute("currentUserId") Long currentUserId) {
        return Result.success(activityInvitationService.getMyInvitations(currentUserId));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getInvitation(@PathVariable Long id,
                                                     @RequestAttribute("currentUserId") Long currentUserId) {
        Map<String, Object> detail = activityInvitationService.getInvitationDetail(id, currentUserId);
        return detail != null ? Result.success(detail) : Result.error(403, "Invitation not found or inaccessible.");
    }

    @PostMapping
    public Result<ActivityInvitation> createInvitation(@RequestBody Map<String, Object> body,
                                                       @RequestAttribute("currentUserId") Long currentUserId) {
        Long activityId = Long.valueOf(body.get("activityId").toString());
        Long receiverId = Long.valueOf(body.get("receiverId").toString());
        return Result.created(activityInvitationService.createInvitation(activityId, currentUserId, receiverId));
    }

    @PostMapping("/{id}/handle")
    public Result<String> handleInvitation(@PathVariable Long id,
                                           @RequestBody Map<String, Boolean> body,
                                           @RequestAttribute("currentUserId") Long currentUserId) {
        boolean success = activityInvitationService.handleInvitation(id, body.getOrDefault("accept", false), currentUserId);
        return success ? Result.success("Invitation handled.") : Result.error(403, "Unable to handle this invitation.");
    }
}
