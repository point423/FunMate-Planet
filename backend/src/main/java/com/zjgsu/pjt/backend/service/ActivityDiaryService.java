package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.ActivityDiary;
import com.zjgsu.pjt.backend.repository.ActivityDiaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ActivityDiaryService {
    @Autowired
    private ActivityDiaryRepository diaryRepository;

    public ActivityDiary saveDiary(ActivityDiary diary) {
        return diaryRepository.save(diary);
    }

    public List<ActivityDiary> getDiariesByUserId(Long userId) {
        return diaryRepository.findByUserId(userId);
    }
}
