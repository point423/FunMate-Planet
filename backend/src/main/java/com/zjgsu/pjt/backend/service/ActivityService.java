package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.ActivityRepository;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public Activity updateActivity(Long id, Activity updated) {
        Activity existing = findById(id);
        if (existing != null) {
            if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
            if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
            if (updated.getActivityTime() != null) existing.setActivityTime(updated.getActivityTime());
            if (updated.getLocation() != null) existing.setLocation(updated.getLocation());
            if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
            return activityRepository.save(existing);
        }
        return null;
    }

    public boolean deleteActivity(Long id) {
        if (activityRepository.existsById(id)) {
            activityRepository.deleteById(id);
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
