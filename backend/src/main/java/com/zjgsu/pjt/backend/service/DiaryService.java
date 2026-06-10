package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.ActivityDiary;
import com.zjgsu.pjt.backend.entity.ActivityParticipant;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.ActivityDiaryRepository;
import com.zjgsu.pjt.backend.repository.ActivityParticipantRepository;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class DiaryService {

    @Autowired
    private ActivityDiaryRepository diaryRepository;

    @Autowired
    private ActivityParticipantRepository activityParticipantRepository;

    @Autowired
    private UserRepository userRepository;

    public ActivityDiary createDiary(ActivityDiary diary) {
        return diaryRepository.save(diary);
    }

    public Page<ActivityDiary> getDiaries(Long userId, Pageable pageable) {
        if (userId != null) {
            return diaryRepository.findByUserId(userId, pageable);
        }
        return diaryRepository.findAll(pageable);
    }

    public Page<Map<String, Object>> getDiariesWithParticipants(Long userId, Pageable pageable) {
        Page<ActivityDiary> diaryPage;
        if (userId != null) {
            diaryPage = diaryRepository.findByUserId(userId, pageable);
        } else {
            diaryPage = diaryRepository.findAll(pageable);
        }
        
        // 为每个日记添加参与者信息
        List<Map<String, Object>> contentWithParticipants = diaryPage.getContent().stream().map(diary -> {
            Map<String, Object> map = new HashMap<>();
            map.put("diary", diary);
            map.put("participants", getParticipantsByDiaryId(diary.getId()));
            return map;
        }).collect(Collectors.toList());
        
        // 创建新的Page对象
        return new PageImpl<>(contentWithParticipants, pageable, diaryPage.getTotalElements());
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

        /**
     * 创建日记并保存参与者信息
     */
    public ActivityDiary createDiaryWithParticipants(ActivityDiary diary, List<Long> participantIds) {
        ActivityDiary savedDiary = diaryRepository.save(diary);
        System.out.println("日记已保存，ID: " + savedDiary.getId() + ", 活动ID: " + savedDiary.getActivityId());
        
        if (participantIds != null && !participantIds.isEmpty()) {
            System.out.println("开始保存参与者信息，数量: " + participantIds.size());
            for (Long userId : participantIds) {
                ActivityParticipant participant = new ActivityParticipant();
                participant.setActivityId(savedDiary.getActivityId());
                participant.setUserId(userId);
                participant.setStatus(1);
                ActivityParticipant savedParticipant = activityParticipantRepository.save(participant);
                System.out.println("参与者已保存: " + savedParticipant.getId());
            }
        } else {
            System.out.println("没有参与者需要保存");
        }
        
        return savedDiary;
    }


    /**
     * 获取日记的参与者列表
     */
    /**
     * 根据日记ID获取参与者列表
     * @param diaryId 日记ID
     * @return 参与者用户列表，如果日记不存在或没有关联活动则返回空列表
     */
    public List<User> getParticipantsByDiaryId(Long diaryId) {
        // 根据ID查找日记
        ActivityDiary diary = findById(diaryId);
        if (diary == null || diary.getActivityId() == null) {
            return List.of();
        }
        
        List<ActivityParticipant> participants = activityParticipantRepository.findByActivityId(diary.getActivityId());
        return participants.stream()
            .map(p -> userRepository.findById(p.getUserId()).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

}
