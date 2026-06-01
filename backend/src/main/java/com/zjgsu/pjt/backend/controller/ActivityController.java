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

    @PutMapping("/{id}")
    public Result<Activity> update(@PathVariable Long id, @RequestBody Activity activity, @RequestAttribute("currentUserId") Long currentUserId) {
        Activity updated = activityService.updateActivity(id, activity, currentUserId);
        if (updated != null) {
            return Result.success(updated);
        }
        return Result.error(403, "只有发起人可以修改活动信息哦");
    }

    @GetMapping
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "0") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize,
                                           @RequestParam(required = false) Integer status) {
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize);
        Page<Activity> page = activityService.getActivities(status, pageRequest);
        
        List<Map<String, Object>> contentWithCount = page.getContent().stream().map(act -> {
            Map<String, Object> map = new HashMap<>();
            map.put("activity", act);
            map.put("joinedCount", activityService.getParticipants(act.getId()).size());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", contentWithCount);
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        
        return Result.success(response);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Activity activity = activityService.findById(id);
        if (activity == null) return Result.error(404, "活动不存在");
        
        List<User> participants = activityService.getParticipants(id);
        Map<String, Object> response = new HashMap<>();
        response.put("activity", activity);
        response.put("participants", participants);
        response.put("participantCount", participants.size());
        
        return Result.success(response);
    }

    @PostMapping("/{id}/join")
    public Result<String> joinActivity(@PathVariable Long id, @RequestAttribute("currentUserId") Long currentUserId) {
        int status = activityService.joinActivity(id, currentUserId);
        if (status == 0) return Result.success("成功加入活动");
        if (status == 1) return Result.error(400, "你已经是该活动的成员了");
        if (status == 3) return Result.error(400, "活动人数已经满了哦");
        return Result.error(400, "加入失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, @RequestAttribute("currentUserId") Long currentUserId) {
        boolean deleted = activityService.deleteActivity(id, currentUserId);
        return deleted ? Result.success("活动已删除") : Result.error(403, "只有发起人可以删除活动哦");
    }
}
