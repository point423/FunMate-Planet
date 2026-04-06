
package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.ActivityDiary;
import com.zjgsu.pjt.backend.repository.ActivityDiaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diaries")
@CrossOrigin(origins = "*")
public class DiaryController {

    @Autowired
    private ActivityDiaryRepository diaryRepository;

    @PostMapping
    public Result<ActivityDiary> createDiary(@RequestBody ActivityDiary diary) {
        return Result.created(diaryRepository.save(diary));
    }

    @GetMapping
    public Result<Page<ActivityDiary>> getDiaries(@RequestParam(required = false) Long userId,
                                                  @RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize) {
        PageRequest pageRequest = PageRequest.of(Math.max(pageNum - 1, 0), pageSize);

        if (userId != null) {
            return Result.success(diaryRepository.findByUserId(userId, pageRequest));
        }
        return Result.success(diaryRepository.findAll(pageRequest));
    }

    @GetMapping("/{id}")
    public Result<ActivityDiary> getDiaryDetail(@PathVariable Long id) {
        return diaryRepository.findById(id)
                .map(Result::success)
                .orElse(Result.error(404, "日记不存在"));
    }

    @PutMapping("/{id}")
    public Result<ActivityDiary> updateDiary(@PathVariable Long id, @RequestBody ActivityDiary diary) {
        return diaryRepository.findById(id)
                .map(existing -> {
                    existing.setContent(diary.getContent());
                    existing.setImages(diary.getImages());
                    existing.setActivityId(diary.getActivityId());
                    return Result.success(diaryRepository.save(existing));
                })
                .orElse(Result.error(404, "日记不存在"));
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteDiary(@PathVariable Long id) {
        if (diaryRepository.existsById(id)) {
            diaryRepository.deleteById(id);
            return Result.success("日记已删除");
        }
        return Result.error(404, "日记不存在");
    }
}
