package com.zjgsu.pjt.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.ActivityDiary;
import com.zjgsu.pjt.backend.entity.ActivityDiaryEntry;
import com.zjgsu.pjt.backend.service.DiaryService;
import com.zjgsu.pjt.backend.service.SharedJournalShowcaseService;
import com.zjgsu.pjt.backend.util.FileStorageUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diaries")
@CrossOrigin(origins = "*")
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SharedJournalShowcaseService sharedJournalShowcaseService;

    @PostMapping
    public Result<ActivityDiary> createDiary(HttpServletRequest request) {
        Object attr = request.getAttribute("currentUserId");
        if (attr == null) return Result.error(401, "Unauthorized");
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

            String participantIdsStr = request.getParameter("participantIds");
            List<Long> participantIds = new ArrayList<>();
            if (participantIdsStr != null && !participantIdsStr.trim().isEmpty()) {
                participantIds = mapper.readValue(participantIdsStr, new TypeReference<List<Long>>() {});
            }

            List<MultipartFile> files = new ArrayList<>();
            if (request instanceof MultipartHttpServletRequest multipartRequest) {
                files = multipartRequest.getFiles("files");
            }

            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = FileStorageUtil.saveFile(file);
                    if (url != null) imageUrls.add(url);
                }
            }

            if (!imageUrls.isEmpty()) {
                diary.setImages(mapper.writeValueAsString(imageUrls));
            } else if (!isJsonRequest(request)) {
                diary.setImages(null);
            }

            ActivityDiary saved = participantIds.isEmpty()
                    ? diaryService.createDiary(diary)
                    : diaryService.createDiaryWithParticipants(diary, participantIds);
            return Result.created(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "Diary save failed: " + e.getMessage());
        }
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    @GetMapping
    public Result<Page<Map<String, Object>>> getDiaries(@RequestParam(required = false) Long userId,
                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize,
                                                        @RequestAttribute("currentUserId") Long currentUserId) {
        Long resolvedUserId = userId != null ? userId : currentUserId;
        PageRequest pageRequest = PageRequest.of(Math.max(pageNum - 1, 0), pageSize);
        return Result.success(diaryService.getDiariesWithParticipants(resolvedUserId, pageRequest));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDiaryDetail(@PathVariable Long id,
                                                      @RequestAttribute("currentUserId") Long currentUserId) {
        ActivityDiary diary = diaryService.findById(id);
        if (diary == null) return Result.error(404, "Diary not found");
        if (!diaryService.canAccessDiary(id, currentUserId)) {
            return Result.error(403, "No permission to view this diary");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("diary", diary);
        response.put("participants", diaryService.getParticipantsByDiaryId(id));
        response.put("participantCount", diaryService.getParticipantsByDiaryId(id).size());
        response.put("sharedEntries", diaryService.getSharedEntriesByDiaryId(id));
        return Result.success(response);
    }

    @PutMapping("/{id}")
    public Result<ActivityDiary> updateDiary(@PathVariable Long id,
                                             @RequestBody ActivityDiary diary,
                                             HttpServletRequest request) {
        Object attr = request.getAttribute("currentUserId");
        if (attr == null) return Result.error(401, "Unauthorized");
        Long currentUserId = Long.valueOf(attr.toString());
        ActivityDiary updated = diaryService.updateDiary(id, diary, currentUserId);
        return updated != null ? Result.success(updated) : Result.error(403, "No permission to update");
    }

    @PutMapping("/{id}/entries/me")
    public Result<Map<String, Object>> updateMySharedEntry(@PathVariable Long id,
                                                           @RequestBody ActivityDiaryEntry entry,
                                                           HttpServletRequest request) {
        Object attr = request.getAttribute("currentUserId");
        if (attr == null) return Result.error(401, "Unauthorized");
        Long currentUserId = Long.valueOf(attr.toString());
        if (!diaryService.canAccessDiary(id, currentUserId)) {
            return Result.error(403, "No permission to update this diary");
        }

        ActivityDiaryEntry updated = diaryService.updateSharedEntry(id, currentUserId, entry);
        if (updated == null) {
            return Result.error(403, "No permission to update or diary not found");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("entry", updated);
        return Result.success(response);
    }

    @PostMapping("/{id}/share-me")
    public Result<Map<String, Object>> shareMyEntry(@PathVariable Long id,
                                                    HttpServletRequest request) {
        Object attr = request.getAttribute("currentUserId");
        if (attr == null) return Result.error(401, "Unauthorized");
        Long currentUserId = Long.valueOf(attr.toString());
        if (!diaryService.canAccessDiary(id, currentUserId)) {
            return Result.error(403, "No permission to access this diary");
        }

        try {
            Map<String, Object> response = new HashMap<>();
            response.put("showcase", sharedJournalShowcaseService.shareMyEntry(id, currentUserId));
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteDiary(@PathVariable Long id, HttpServletRequest request) {
        Object attr = request.getAttribute("currentUserId");
        if (attr == null) return Result.error(401, "Unauthorized");
        Long currentUserId = Long.valueOf(attr.toString());
        boolean success = diaryService.deleteDiary(id, currentUserId);
        return success ? Result.success("Diary deleted") : Result.error(403, "No permission to delete");
    }
}
