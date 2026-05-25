package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.entity.ActivityParticipant;
import com.zjgsu.pjt.backend.entity.ActivityReview;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.ActivityParticipantRepository;
import com.zjgsu.pjt.backend.repository.ActivityRepository;
import com.zjgsu.pjt.backend.repository.ActivityReviewRepository;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityParticipantRepository participantRepository;

    @Autowired
    private ActivityReviewRepository reviewRepository;

    public List<Map<String, Object>> getTopParticipants() {
        List<ActivityReview> allReviews = reviewRepository.findAll();
        Map<Long, List<ActivityReview>> grouped = allReviews.stream()
                .collect(Collectors.groupingBy(ActivityReview::getRevieweeId));

        return grouped.entrySet().stream()
                .map(entry -> {
                    Long userId = entry.getKey();
                    List<ActivityReview> userReviews = entry.getValue();
                    double avgRating = userReviews.stream().mapToInt(ActivityReview::getRating).average().orElse(0);
                    User user = userRepository.findById(userId).orElse(null);

                    Map<String, Object> map = new HashMap<>();
                    map.put("id", userId);
                    map.put("nickname", user != null ? user.getNickname() : "神秘用户");
                    map.put("avatar", user != null ? user.getAvatar() : "");
                    map.put("score", Math.round(avgRating * 10.0) / 10.0);
                    map.put("reviewCount", userReviews.size());
                    return map;
                })
                .sorted((a, b) -> Double.compare((double) b.get("score"), (double) a.get("score")))
                .limit(10)
                .collect(Collectors.toList());
    }

    @Transactional
    public Activity createActivity(Activity activity) {
        Activity saved = activityRepository.save(activity);
        internalJoin(saved.getId(), saved.getCreatorId());
        return saved;
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
            if (updated.getMaxParticipants() != null) existing.setMaxParticipants(updated.getMaxParticipants());
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

    @Transactional
    public int joinActivity(Long activityId, Long currentUserId) {
        Activity activity = findById(activityId);
        if (activity == null || activity.getStatus() != 0) {
            return 2;
        }

        if (participantRepository.findByActivityIdAndUserId(activityId, currentUserId).isPresent()) {
            return 1;
        }
        
        List<ActivityParticipant> currentParticipants = participantRepository.findByActivityId(activityId);
        if (activity.getMaxParticipants() != null && currentParticipants.size() >= activity.getMaxParticipants()) {
            return 3;
        }

        ActivityParticipant participant = new ActivityParticipant();
        participant.setActivityId(activityId);
        participant.setUserId(currentUserId);
        participantRepository.save(participant);
        return 0;
    }

    @Transactional
    public boolean endActivity(Long activityId, Long currentUserId) {
        Activity activity = findById(activityId);
        if (activity != null && Objects.equals(activity.getCreatorId(), currentUserId)) {
            activity.setStatus(2);
            activityRepository.save(activity);
            return true;
        }
        return false;
    }

    private void internalJoin(Long activityId, Long userId) {
        if (participantRepository.findByActivityIdAndUserId(activityId, userId).isEmpty()) {
            ActivityParticipant participant = new ActivityParticipant();
            participant.setActivityId(activityId);
            participant.setUserId(userId);
            participantRepository.save(participant);
        }
    }

    public List<User> getParticipants(Long activityId) {
        List<ActivityParticipant> participants = participantRepository.findByActivityId(activityId);
        List<Long> userIds = participants.stream()
                .map(ActivityParticipant::getUserId)
                .collect(Collectors.toList());
        return userRepository.findAllById(userIds);
    }
}
