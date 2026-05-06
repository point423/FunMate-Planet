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

    @PostMapping
    public Result<ActivityDiary> createDiary(@RequestBody ActivityDiary diary, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        if (currentUserId == null) return Result.error(401, "未授权");
        
        diary.setUserId(currentUserId);
        return Result.created(diaryService.createDiary(diary));
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
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        if (currentUserId == null) return Result.error(401, "未授权");

        ActivityDiary updated = diaryService.updateDiary(id, diary, currentUserId);
        if (updated == null) {
            return Result.error(403, "无权修改此日记或日记不存在");
        }
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteDiary(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        if (currentUserId == null) return Result.error(401, "未授权");

        boolean success = diaryService.deleteDiary(id, currentUserId);
        return success ? Result.success("日记已删除") : Result.error(403, "无权删除此日记或日记不存在");
    }
}
