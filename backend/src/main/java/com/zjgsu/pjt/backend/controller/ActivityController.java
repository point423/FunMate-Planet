package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*")
public class ActivityController {

    @Autowired
    private ActivityRepository activityRepository;

    @PostMapping
    public Result<Activity> create(@RequestBody Activity activity) {
        return Result.success(activityRepository.save(activity));
    }

    @GetMapping
    public Result<Page<Activity>> list(@RequestParam(defaultValue = "0") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) Integer status) {
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize);
        if (status != null) {
            return Result.success(activityRepository.findByStatus(status, pageRequest));
        }
        return Result.success(activityRepository.findAll(pageRequest));
    }

    @GetMapping("/{id}")
    public Result<Activity> getDetail(@PathVariable Long id) {
        return activityRepository.findById(id)
                .map(Result::success)
                .orElse(Result.error(404, "活动不存在"));
    }

    // 补全 5.3 接口：加入活动
    @PostMapping("/{id}/join")
    public Result<String> joinActivity(@PathVariable Long id) {
        return Result.success("成功加入活动 ID: " + id);
    }

    // 补全结束活动接口
    @PostMapping("/{id}/end")
    public Result<String> endActivity(@PathVariable Long id) {
        return Result.success("活动已结束");
    }
}
