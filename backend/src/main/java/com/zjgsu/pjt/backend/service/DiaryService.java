package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.ActivityDiary;
import com.zjgsu.pjt.backend.repository.ActivityDiaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DiaryService {

    @Autowired
    private ActivityDiaryRepository diaryRepository;

    public ActivityDiary createDiary(ActivityDiary diary) {
        return diaryRepository.save(diary);
    }

    public List<ActivityDiary> getDiaries(Long userId) {
        if (userId != null) {
            return diaryRepository.findByUserId(userId);
        }
        return diaryRepository.findAll();
    }

    public ActivityDiary findById(Long id) {
        return diaryRepository.findById(id).orElse(null);
    }

    /**
     * 补全：更新日记
     */
    public ActivityDiary updateDiary(Long id, ActivityDiary updated) {
        ActivityDiary existing = findById(id);
        if (existing != null) {
            if (updated.getContent() != null) existing.setContent(updated.getContent());
            if (updated.getImages() != null) existing.setImages(updated.getImages());
            if (updated.getTags() != null) existing.setTags(updated.getTags());
            return diaryRepository.save(existing);
        }
        return null;
    }

    /**
     * 补全：删除日记
     */
    public boolean deleteDiary(Long id) {
        if (diaryRepository.existsById(id)) {
            diaryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
