package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @PostMapping
    public Result<Activity> create(@RequestBody Activity activity, @RequestAttribute("currentUserId") Long currentUserId) {
        activity.setCreatorId(currentUserId);
        return Result.created(activityService.createActivity(activity));
    }

    @PostMapping("/{id}/invite")
    public Result<Activity> inviteFriend(@PathVariable Long id,
                                         @RequestBody Map<String, Object> body,
                                         @RequestAttribute("currentUserId") Long currentUserId) {
        Long inviteeId = Long.valueOf(body.get("inviteeId").toString());
        return Result.success(activityService.inviteFriend(id, currentUserId, inviteeId));
    }

    @PutMapping("/{id}")
    public Result<Activity> update(@PathVariable Long id, @RequestBody Activity activity, @RequestAttribute("currentUserId") Long currentUserId) {
        Activity updated = activityService.updateActivity(id, activity, currentUserId);
        if (updated != null) {
            return Result.success(updated);
        }
        return Result.error(403, "Only the creator can update this activity.");
    }

    @GetMapping
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "0") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) Integer status) {
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize);
        Page<Activity> page = activityService.getActivities(status, pageRequest);

        List<Map<String, Object>> contentWithCount = page.getContent().stream().map(activity -> {
            Map<String, Object> map = new HashMap<>();
            map.put("activity", activity);
            map.put("joinedCount", activityService.getParticipants(activity.getId()).size());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", contentWithCount);
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());

        return Result.success(response);
    }

    @GetMapping("/my")
    public Result<Map<String, List<Activity>>> myActivities(@RequestAttribute("currentUserId") Long currentUserId) {
        return new Result<>(200, activityService.getMyActivities(currentUserId), "success");
    }

    @GetMapping("/completable")
    public Result<List<Activity>> completableActivities(@RequestAttribute("currentUserId") Long currentUserId) {
        return Result.success(activityService.getCompletableActivities(currentUserId));
    }

    @GetMapping("/leaderboard")
    public Result<List<Map<String, Object>>> leaderboard() {
        return Result.success(activityService.getTopParticipants());
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Activity activity = activityService.findById(id);
        if (activity == null) {
            return Result.error(404, "Activity not found.");
        }

        List<User> participants = activityService.getParticipants(id);
        Map<String, Object> response = new HashMap<>();
        response.put("activity", activity);
        response.put("participants", participants);
        response.put("participantCount", participants.size());
        response.put("hasJournal", activityService.hasJournal(id));

        return Result.success(response);
    }

    @PostMapping("/{id}/join")
    public Result<String> joinActivity(@PathVariable Long id, @RequestAttribute("currentUserId") Long currentUserId) {
        int status = activityService.joinActivity(id, currentUserId);
        if (status == 0) return Result.success("Joined activity successfully.");
        if (status == 1) return Result.error(400, "You are already part of this activity.");
        if (status == 3) return Result.error(400, "This activity is already full.");
        return Result.error(400, "Unable to join the activity.");
    }

    @PostMapping("/{id}/complete")
    public Result<String> complete(@PathVariable Long id, @RequestAttribute("currentUserId") Long currentUserId) {
        return activityService.completeActivity(id, currentUserId)
                ? Result.success("Activity completed.")
                : Result.error(403, "Unable to complete this activity.");
    }

    @PostMapping("/{id}/end")
    public Result<String> end(@PathVariable Long id, @RequestAttribute("currentUserId") Long currentUserId) {
        return activityService.completeActivity(id, currentUserId)
                ? Result.success("Activity completed.")
                : Result.error(403, "Unable to complete this activity.");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, @RequestAttribute("currentUserId") Long currentUserId) {
        boolean deleted = activityService.deleteActivity(id, currentUserId);
        return deleted ? Result.success("Activity deleted.") : Result.error(403, "Only the creator can delete this activity.");
    }
}
