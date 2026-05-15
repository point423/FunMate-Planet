package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public Result<Page<Activity>> list(@RequestParam(defaultValue = "0") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) Integer status) {
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize);
        return Result.success(activityService.getActivities(status, pageRequest));
    }

    @PostMapping("/{id}/join")
    public Result<String> joinActivity(@PathVariable Long id, @RequestAttribute("currentUserId") Long currentUserId) {
        boolean success = activityService.joinActivity(id, currentUserId);
        return success ? Result.success("成功加入活动") : Result.error(400, "加入失败，活动可能已结束或不存在");
    }

    @PostMapping("/{id}/end")
    public Result<String> endActivity(@PathVariable Long id, @RequestAttribute("currentUserId") Long currentUserId) {
        boolean success = activityService.endActivity(id, currentUserId);
        return success ? Result.success("活动已结束，现在可以去发布日记和评价搭子了") : Result.error(403, "无权结束此活动");
    }

    @GetMapping("/{id}/participants")
    public Result<List<User>> getParticipants(@PathVariable Long id) {
        return Result.success(activityService.getParticipants(id));
    }
}
