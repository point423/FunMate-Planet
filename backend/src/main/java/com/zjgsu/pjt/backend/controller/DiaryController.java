package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.ActivityDiary;
import com.zjgsu.pjt.backend.service.DiaryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diaries")
@CrossOrigin(origins = "*")
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

    /**
     * 终极修复：物理级绕过 Spring 的 Content-Type 检查
     * 彻底解决 "multipart/form-data;...;charset=UTF-8 is not supported"
     */
    @PostMapping
    public Result<ActivityDiary> createDiary(HttpServletRequest request) {
        // 1. 获取登录用户
        Object attr = request.getAttribute("currentUserId");
        if (attr == null) return Result.error(401, "未授权");
        Long currentUserId = Long.valueOf(attr.toString());

        try {
            // 2. 手动构造对象。getParameter 方法会自动触发表单解析
            // 即使 Content-Type 包含非标的 charset，Servlet 容器通常也能兼容处理
            ActivityDiary diary = new ActivityDiary();
            diary.setUserId(currentUserId);
            diary.setContent(request.getParameter("content"));
            diary.setImages(request.getParameter("images"));
            diary.setTags(request.getParameter("tags"));
            
            String aid = request.getParameter("activityId");
            if (aid != null && !aid.trim().isEmpty()) {
                diary.setActivityId(Long.valueOf(aid));
            }

            // 3. 调用 Service
            ActivityDiary saved = diaryService.createDiary(diary);
            return Result.created(saved);
        } catch (Exception e) {
            return Result.error(500, "日记保存失败（系统异常）: " + e.getMessage());
        }
    }

    @GetMapping
    public Result<Page<ActivityDiary>> getDiaries(@RequestParam(required = false) Long userId,
                                                  @RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize) {
        PageRequest pageRequest = PageRequest.of(Math.max(pageNum - 1, 0), pageSize);
        return Result.success(diaryService.getDiaries(userId, pageRequest));
    }

    @GetMapping("/{id}")
    public Result<ActivityDiary> getDiaryDetail(@PathVariable Long id) {
        ActivityDiary diary = diaryService.findById(id);
        return diary != null ? Result.success(diary) : Result.error(404, "日记不存在");
    }

    @PutMapping("/{id}")
    public Result<ActivityDiary> updateDiary(@PathVariable Long id,
                                             @RequestBody ActivityDiary diary,
                                             HttpServletRequest request) {
        Object attr = request.getAttribute("currentUserId");
        if (attr == null) return Result.error(401, "未授权");
        Long currentUserId = Long.valueOf(attr.toString());
        ActivityDiary updated = diaryService.updateDiary(id, diary, currentUserId);
        return updated != null ? Result.success(updated) : Result.error(403, "无权修改");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteDiary(@PathVariable Long id, HttpServletRequest request) {
        Object attr = request.getAttribute("currentUserId");
        if (attr == null) return Result.error(401, "未授权");
        Long currentUserId = Long.valueOf(attr.toString());
        boolean success = diaryService.deleteDiary(id, currentUserId);
        return success ? Result.success("日记已删除") : Result.error(403, "无权删除");
    }
}
