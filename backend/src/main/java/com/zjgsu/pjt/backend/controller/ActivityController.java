package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @PostMapping
    public Result<Activity> create(@RequestBody Activity activity) {
        return Result.created(activityService.createActivity(activity));
    }

    @GetMapping
    public Result<Page<Activity>> list(@RequestParam(defaultValue = "0") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) Integer status) {
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize);
        return Result.success(activityService.getActivities(status, pageRequest));
    }

    @GetMapping("/{id}")
    public Result<Activity> getDetail(@PathVariable Long id) {
        Activity activity = activityService.findById(id);
        return activity != null ? Result.success(activity) : Result.error(404, "活动不存在");
    }

    @PutMapping("/{id}")
    public Result<Activity> update(@PathVariable Long id, @RequestBody Activity activity) {
        Activity updated = activityService.updateActivity(id, activity);
        return updated != null ? Result.success(updated) : Result.error(404, "活动不存在");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = activityService.deleteActivity(id);
        return success ? Result.success("活动已删除") : Result.error(404, "活动不存在");
    }

    @PostMapping("/{id}/join")
    public Result<String> joinActivity(@PathVariable Long id) {
        return Result.created("成功加入活动 ID: " + id);
    }

    @GetMapping("/{id}/participants")
    public Result<List<User>> getParticipants(@PathVariable Long id) {
        List<User> participants = activityService.getParticipants(id);
        return Result.success(participants != null ? participants : new ArrayList<>());
    }

    @PostMapping("/{id}/end")
    public Result<String> endActivity(@PathVariable Long id) {
        return Result.created("活动已结束");
    }
}
