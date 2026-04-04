// package com.zjgsu.pjt.backend.controller;

// import com.zjgsu.pjt.backend.common.Result;
// import com.zjgsu.pjt.backend.entity.ActivityDiary;
// import com.zjgsu.pjt.backend.repository.ActivityDiaryRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api/diaries")
// @CrossOrigin(origins = "*")
// public class DiaryController {

//     @Autowired
//     private ActivityDiaryRepository diaryRepository;

//     @PostMapping
//     public Result<ActivityDiary> createDiary(@RequestBody ActivityDiary diary) {
//         return Result.success(diaryRepository.save(diary));
//     }

//     @GetMapping
//     public Result<List<ActivityDiary>> getDiaries(@RequestParam(required = false) Long userId) {
//         if (userId != null) {
//             return Result.success(diaryRepository.findByUserId(userId));
//         }
//         return Result.success(diaryRepository.findAll());
//     }

//     @GetMapping("/{id}")
//     public Result<ActivityDiary> getDiaryDetail(@PathVariable Long id) {
//         return diaryRepository.findById(id)
//                 .map(Result::success)
//                 .orElse(Result.error(404, "日记不存在"));
//     }
// }

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
}