package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.entity.ActivityParticipant;
import com.zjgsu.pjt.backend.entity.ActivityReview;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.ActivityDiaryRepository;
import com.zjgsu.pjt.backend.repository.ActivityParticipantRepository;
import com.zjgsu.pjt.backend.repository.ActivityRepository;
import com.zjgsu.pjt.backend.repository.ActivityReviewRepository;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    @Autowired
    private ActivityDiaryRepository diaryRepository;

    @Autowired
    private ActivityInvitationService activityInvitationService;

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
                    map.put("nickname", user != null ? user.getNickname() : "Unknown user");
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
        if (activity.getStatus() == null) {
            activity.setStatus(0);
        }
        Activity saved = activityRepository.save(activity);
        internalJoin(saved.getId(), saved.getCreatorId());

        if (activity.getInviteeId() != null) {
            activityInvitationService.createInvitation(saved.getId(), saved.getCreatorId(), activity.getInviteeId());
        }

        return saved;
    }

    @Transactional
    public Activity inviteFriend(Long activityId, Long creatorId, Long inviteeId) {
        Activity activity = findById(activityId);
        if (activity == null) {
            throw new IllegalArgumentException("Activity does not exist.");
        }

        if (!Objects.equals(activity.getCreatorId(), creatorId)) {
            throw new IllegalArgumentException("Only the creator can send invitations.");
        }

        activityInvitationService.createInvitation(activityId, creatorId, inviteeId);
        return activity;
    }

    public Page<Activity> getActivities(Integer status, Pageable pageable) {
        if (status != null) {
            return activityRepository.findByStatus(status, pageable);
        }
        return activityRepository.findAll(pageable);
    }

    public Map<String, List<Activity>> getMyActivities(Long userId) {
        Map<String, List<Activity>> grouped = newActivityGroupMap();

        List<ActivityParticipant> participantRecords = participantRepository.findByUserId(userId);
        Set<Long> activityIds = participantRecords.stream()
                .map(ActivityParticipant::getActivityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, Activity> uniqueActivities = new LinkedHashMap<>();
        if (!activityIds.isEmpty()) {
            activityRepository.findByIdIn(activityIds).forEach(activity -> {
                if (activity.getId() != null) {
                    uniqueActivities.put(activity.getId(), activity);
                }
            });
        }

        activityInvitationService.getPendingIncomingInvitations(userId).forEach(invitation -> {
            Activity invitedActivity = findById(invitation.getActivityId());
            if (invitedActivity != null && invitedActivity.getId() != null) {
                uniqueActivities.putIfAbsent(invitedActivity.getId(), invitedActivity);
            }
        });

        activityRepository.findByCreatorIdAndStatus(userId, 0).forEach(activity -> {
            if (activity.getId() != null) {
                uniqueActivities.putIfAbsent(activity.getId(), activity);
            }
        });

        uniqueActivities.values().forEach(activity ->
                grouped.get(resolveActivityGroup(activity.getStatus())).add(activity)
        );

        return grouped;
    }

    public List<Activity> getCompletableActivities(Long userId) {
        List<ActivityParticipant> participantRecords = participantRepository.findByUserId(userId);
        Set<Long> activityIds = participantRecords.stream()
                .map(ActivityParticipant::getActivityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, Activity> activities = new LinkedHashMap<>();
        if (!activityIds.isEmpty()) {
            activityRepository.findByIdIn(activityIds).forEach(activity -> {
                if (activity.getId() != null && activity.getStatus() != null && activity.getStatus() == 2 && !hasJournal(activity.getId())) {
                    activities.put(activity.getId(), activity);
                }
            });
        }

        activityRepository.findByCreatorIdAndStatus(userId, 2).forEach(activity -> {
            if (activity.getId() != null && !hasJournal(activity.getId())) {
                activities.putIfAbsent(activity.getId(), activity);
            }
        });

        return new ArrayList<>(activities.values());
    }

    public Activity findById(Long id) {
        return activityRepository.findById(id).orElse(null);
    }

    public boolean hasJournal(Long activityId) {
        return diaryRepository.existsByActivityId(activityId);
    }

    @Transactional
    public Activity updateActivity(Long id, Activity updated, Long currentUserId) {
        Activity existing = findById(id);
        if (existing != null && Objects.equals(existing.getCreatorId(), currentUserId)) {
            if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
            if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
            if (updated.getPlan() != null) existing.setPlan(updated.getPlan());
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
        activity.setStatus(1);
        activityRepository.save(activity);
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

    @Transactional
    public boolean completeActivity(Long activityId, Long currentUserId) {
        Activity activity = findById(activityId);
        if (activity == null || activity.getStatus() == null || activity.getStatus() != 1) {
            return false;
        }

        boolean isCreator = Objects.equals(activity.getCreatorId(), currentUserId);
        boolean isParticipant = participantRepository.findByActivityIdAndUserId(activityId, currentUserId).isPresent();
        if (!isCreator && !isParticipant) {
            return false;
        }

        activity.setStatus(2);
        activityRepository.save(activity);
        return true;
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

    private Map<String, List<Activity>> newActivityGroupMap() {
        Map<String, List<Activity>> grouped = new LinkedHashMap<>();
        grouped.put("pending", new ArrayList<>());
        grouped.put("active", new ArrayList<>());
        grouped.put("completed", new ArrayList<>());
        grouped.put("cancelled", new ArrayList<>());
        return grouped;
    }

    private String resolveActivityGroup(Integer status) {
        if (status == null || status == 0) {
            return "pending";
        }
        if (status == 1) {
            return "active";
        }
        if (status == 2) {
            return "completed";
        }
        return "cancelled";
    }
}
