package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.ActivityDiary;
import com.zjgsu.pjt.backend.repository.ActivityDiaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
public class DiaryService {

    @Autowired
    private ActivityDiaryRepository diaryRepository;

    public ActivityDiary createDiary(ActivityDiary diary) {
        return diaryRepository.save(diary);
    }

    public Page<ActivityDiary> getDiaries(Long userId, Pageable pageable) {
        if (userId != null) {
            return diaryRepository.findByUserId(userId, pageable);
        }
        return diaryRepository.findAll(pageable);
    }

    public ActivityDiary findById(Long id) {
        return diaryRepository.findById(id).orElse(null);
    }

    /**
     * 安全加固：更新日记（增加 IDOR 校验）
     */
    public ActivityDiary updateDiary(Long id, ActivityDiary updated, Long currentUserId) {
        ActivityDiary existing = findById(id);
        if (existing != null) {
            // 只有日记作者可以修改
            if (!Objects.equals(existing.getUserId(), currentUserId)) {
                return null;
            }
            if (updated.getContent() != null) existing.setContent(updated.getContent());
            if (updated.getImages() != null) existing.setImages(updated.getImages());
            if (updated.getTags() != null) existing.setTags(updated.getTags());
            return diaryRepository.save(existing);
        }
        return null;
    }

    /**
     * 安全加固：删除日记（增加 IDOR 校验）
     */
    public boolean deleteDiary(Long id, Long currentUserId) {
        ActivityDiary existing = findById(id);
        if (existing != null) {
            if (!Objects.equals(existing.getUserId(), currentUserId)) {
                return false;
            }
            diaryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
