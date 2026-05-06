package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.ActivityRepository;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    public Activity createActivity(Activity activity) {
        return activityRepository.save(activity);
    }

    public Page<Activity> getActivities(Integer status, Pageable pageable) {
        if (status != null) {
            return activityRepository.findByStatus(status, pageable);
        }
        return activityRepository.findAll(pageable);
    }

    public Activity findById(Long id) {
        return activityRepository.findById(id).orElse(null);
    }

    @Transactional
    public Activity updateActivity(Long id, Activity updated, Long currentUserId) {
        Activity existing = findById(id);
        if (existing != null && Objects.equals(existing.getCreatorId(), currentUserId)) {
            if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
            if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
            if (updated.getActivityTime() != null) existing.setActivityTime(updated.getActivityTime());
            if (updated.getLocation() != null) existing.setLocation(updated.getLocation());
            if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
            return activityRepository.save(existing);
        }
        return null;
    }

    @Transactional
    public boolean deleteActivity(Long id, Long currentUserId) {
        Activity existing = findById(id);
        if (existing != null && Objects.equals(existing.getCreatorId(), currentUserId)) {
            activityRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean joinActivity(Long activityId, Long currentUserId) {
        Activity activity = findById(activityId);
        // 此处逻辑可扩展：检查是否已加入、活动是否结束等
        return activity != null;
    }

    @Transactional
    public boolean endActivity(Long activityId, Long currentUserId) {
        Activity activity = findById(activityId);
        if (activity != null && Objects.equals(activity.getCreatorId(), currentUserId)) {
            activity.setStatus(2); // 2: 已结束
            activityRepository.save(activity);
            return true;
        }
        return false;
    }

    public List<User> getParticipants(Long activityId) {
        Activity activity = findById(activityId);
        if (activity != null && activity.getCreatorId() != null) {
            User creator = userRepository.findById(activity.getCreatorId()).orElse(null);
            if (creator != null) {
                List<User> participants = new ArrayList<>();
                participants.add(creator);
                return participants;
            }
        }
        return new ArrayList<>();
    }
}
