package com.zjgsu.pjt.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.ActivityDiary;
import com.zjgsu.pjt.backend.service.DiaryService;
import com.zjgsu.pjt.backend.util.FileStorageUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/diaries")
@CrossOrigin(origins = "*")
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 终极修复：物理级绕过 Spring 的 Content-Type 检查
     * 彻底解决 "multipart/form-data;...;charset=UTF-8 is not supported"
     */
    // @PostMapping
    // public Result<ActivityDiary> createDiary(HttpServletRequest request) {
    //     Object attr = request.getAttribute("currentUserId");
    //     if (attr == null) return Result.error(401, "未授权");
    //     Long currentUserId = Long.valueOf(attr.toString());

    //     try {
    //         ActivityDiary diary = new ActivityDiary();
    //         diary.setUserId(currentUserId);
    //         diary.setContent(request.getParameter("content"));
    //         diary.setTitle(request.getParameter("title"));
    //         diary.setTags(request.getParameter("tags"));
            
    //         String aid = request.getParameter("activityId");
    //         if (aid != null && !aid.trim().isEmpty()) {
    //             diary.setActivityId(Long.valueOf(aid));
    //         }

    //         // --- 新增：处理上传的图片文件 ---
    //         List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("files");
    //         List<String> imageUrls = new ArrayList<>();
            
    //         if (files != null && !files.isEmpty()) {
    //             for (MultipartFile file : files) {
    //                 if (!file.isEmpty()) {
    //                     // 调用文件存储工具保存文件并获取 URL
    //                     String url = FileStorageUtil.saveFile(file);
    //                     if (url != null) {
    //                         imageUrls.add(url);
    //                     }
    //                 }
    //             }
    //         }

    //         // 将 URL 列表转为 JSON 字符串存储
    //         if (!imageUrls.isEmpty()) {
    //             ObjectMapper mapper = new ObjectMapper();
    //             diary.setImages(mapper.writeValueAsString(imageUrls));
    //         } else {
    //             diary.setImages(null);
    //         }
    //         // --------------------------------

    //         ActivityDiary saved = diaryService.createDiary(diary);
    //         return Result.created(saved);
    //     } catch (Exception e) {
    //         return Result.error(500, "日记保存失败（系统异常）: " + e.getMessage());
    //     }
    // }

    @PostMapping
    public Result<ActivityDiary> createDiary(HttpServletRequest request) {
        Object attr = request.getAttribute("currentUserId");
        if (attr == null) return Result.error(401, "未授权");
        Long currentUserId = Long.valueOf(attr.toString());

        try {
            ObjectMapper mapper = objectMapper;
            ActivityDiary diary = new ActivityDiary();
            diary.setUserId(currentUserId);

            if (isJsonRequest(request)) {
                ActivityDiary requestBody = mapper.readValue(request.getInputStream(), ActivityDiary.class);
                diary.setContent(requestBody.getContent());
                diary.setTitle(requestBody.getTitle());
                diary.setTags(requestBody.getTags());
                diary.setImages(requestBody.getImages());
                diary.setActivityId(requestBody.getActivityId());
            } else {
                diary.setContent(request.getParameter("content"));
                diary.setTitle(request.getParameter("title"));
                diary.setTags(request.getParameter("tags"));

                String aid = request.getParameter("activityId");
                if (aid != null && !aid.trim().isEmpty()) {
                    diary.setActivityId(Long.valueOf(aid));
                }
            }

            // 处理参与者ID列表
            String participantIdsStr = request.getParameter("participantIds");
            System.out.println("接收到的参与者ID字符串: " + participantIdsStr);
            
            List<Long> participantIds = new ArrayList<>();
            if (participantIdsStr != null && !participantIdsStr.trim().isEmpty()) {
                participantIds = mapper.readValue(participantIdsStr, new TypeReference<List<Long>>(){});
                System.out.println("解析后的参与者ID列表: " + participantIds);
            }

            // 处理上传的图片文件
            List<MultipartFile> files = new ArrayList<>();
            if (request instanceof MultipartHttpServletRequest multipartRequest) {
                files = multipartRequest.getFiles("files");
            }
            List<String> imageUrls = new ArrayList<>();
            
            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String url = FileStorageUtil.saveFile(file);
                        if (url != null) {
                            imageUrls.add(url);
                        }
                    }
                }
            }

            // 将 URL 列表转为 JSON 字符串存储
            if (!imageUrls.isEmpty()) {
                diary.setImages(mapper.writeValueAsString(imageUrls));
            } else if (!isJsonRequest(request)) {
                diary.setImages(null);
            }

            // 创建日记并保存参与者信息
            ActivityDiary saved = participantIds.isEmpty()
                    ? diaryService.createDiary(diary)
                    : diaryService.createDiaryWithParticipants(diary, participantIds);
            System.out.println("保存后的日记ID: " + saved.getId());
            return Result.created(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "日记保存失败（系统异常）: " + e.getMessage());
        }
    }


    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }


    @GetMapping
    public Result<Page<Map<String, Object>>> getDiaries(@RequestParam(required = false) Long userId,
                                                    @RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "10") int pageSize) {
        PageRequest pageRequest = PageRequest.of(Math.max(pageNum - 1, 0), pageSize);
        return Result.success(diaryService.getDiariesWithParticipants(userId, pageRequest));
    }



    // @GetMapping("/{id}")
    // public Result<ActivityDiary> getDiaryDetail(@PathVariable Long id) {
    //     ActivityDiary diary = diaryService.findById(id);
    //     return diary != null ? Result.success(diary) : Result.error(404, "日记不存在");
    // }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDiaryDetail(@PathVariable Long id) {
        ActivityDiary diary = diaryService.findById(id);
        if (diary == null) return Result.error(404, "日记不存在");
        
        Map<String, Object> response = new HashMap<>();
        response.put("diary", diary);
        response.put("participants", diaryService.getParticipantsByDiaryId(id));
        response.put("participantCount", diaryService.getParticipantsByDiaryId(id).size());
        
        return Result.success(response);
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
