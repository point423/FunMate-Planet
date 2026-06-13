package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.dto.UserProfileResponse;
import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.entity.ActivityParticipant;
import com.zjgsu.pjt.backend.entity.SharedJournalShowcase;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.ActivityParticipantRepository;
import com.zjgsu.pjt.backend.repository.ActivityRepository;
import com.zjgsu.pjt.backend.repository.SharedJournalShowcaseRepository;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SharedJournalShowcaseRepository sharedJournalShowcaseRepository;

    @Autowired
    private ActivityParticipantRepository activityParticipantRepository;

    @Autowired
    private ActivityRepository activityRepository;

    private static final String GEO_KEY = "user:location";

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User findEntityById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public UserProfileResponse findById(Long id) {
        User user = findEntityById(id);
        return user != null ? buildProfile(user) : null;
    }

    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::buildProfile)
                .toList();
    }

    public User updateUser(Long id, User user) {
        User existing = findEntityById(id);
        if (existing != null) {
            if (user.getNickname() != null) existing.setNickname(user.getNickname());
            if (user.getAvatar() != null) existing.setAvatar(user.getAvatar());
            if (user.getTags() != null) existing.setTags(user.getTags());
            if (user.getBio() != null) existing.setBio(user.getBio());
            if (user.getAge() != null) existing.setAge(user.getAge());
            if (user.getGender() != null) existing.setGender(user.getGender());
            return userRepository.save(existing);
        }
        return null;
    }

    public User updateProfile(Long id, User profile) {
        User user = findEntityById(id);
        if (user != null) {
            if (profile.getNickname() != null) user.setNickname(profile.getNickname());
            if (profile.getAvatar() != null) user.setAvatar(profile.getAvatar());
            if (profile.getTags() != null) user.setTags(profile.getTags());
            if (profile.getBio() != null) user.setBio(profile.getBio());
            if (profile.getAge() != null) user.setAge(profile.getAge());
            if (profile.getGender() != null) user.setGender(profile.getGender());
            return userRepository.save(user);
        }
        return null;
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            stringRedisTemplate.opsForGeo().remove(GEO_KEY, id.toString());
            return true;
        }
        return false;
    }

    public void updateLocation(Long id, Double lng, Double lat) {
        User user = findEntityById(id);
        if (user != null) {
            user.setLongitude(lng);
            user.setLatitude(lat);
            userRepository.save(user);
            stringRedisTemplate.opsForGeo().add(GEO_KEY, new Point(lng, lat), id.toString());
        }
    }

    private UserProfileResponse buildProfile(User user) {
        UserProfileResponse profile = new UserProfileResponse();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setNickname(user.getNickname());
        profile.setAvatar(user.getAvatar());
        profile.setBio(user.getBio());
        profile.setLongitude(user.getLongitude());
        profile.setLatitude(user.getLatitude());
        profile.setCreatedAt(user.getCreateTime() != null ? user.getCreateTime().toString() : "");
        profile.setScore(user.getAverageScore() != null ? user.getAverageScore() : 0.0);
        profile.setTags(parseTags(user.getTags()));
        profile.setPublicJournals(buildPublicJournals(user.getId()));
        profile.setRecentActivities(buildRecentActivities(user.getId()));
        profile.setActivities(profile.getRecentActivities().size());
        return profile;
    }

    private List<String> parseTags(String rawTags) {
        if (rawTags == null || rawTags.isBlank()) return List.of();
        return List.of(rawTags.split(",")).stream()
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();
    }

    private List<Map<String, Object>> buildPublicJournals(Long userId) {
        return sharedJournalShowcaseRepository.findByUserIdOrderByCreateTimeDesc(userId).stream()
                .map(this::toJournalThumb)
                .toList();
    }

    private Map<String, Object> toJournalThumb(SharedJournalShowcase showcase) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", showcase.getDiaryId());
        item.put("coverImage", showcase.getCoverImage());
        item.put("title", showcase.getTitle());
        item.put("excerpt", showcase.getExcerpt());
        item.put("sharedEntryId", showcase.getDiaryEntryId());
        return item;
    }

    private List<Map<String, Object>> buildRecentActivities(Long userId) {
        List<ActivityParticipant> records = activityParticipantRepository.findByUserId(userId);
        Set<Long> activityIds = new LinkedHashSet<>();
        for (ActivityParticipant record : records) {
            if (record.getActivityId() != null) {
                activityIds.add(record.getActivityId());
            }
        }

        return activityRepository.findByIdIn(activityIds).stream()
                .sorted(Comparator.comparing(Activity::getActivityTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(activity -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", activity.getId());
                    item.put("icon", "•");
                    item.put("name", activity.getTitle());
                    item.put("date", activity.getActivityTime() != null ? activity.getActivityTime().toString() : "");
                    item.put("participants", buildActivityParticipantAvatars(activity.getId()));
                    return item;
                })
                .toList();
    }

    private List<String> buildActivityParticipantAvatars(Long activityId) {
        List<String> avatars = new ArrayList<>();
        for (ActivityParticipant participant : activityParticipantRepository.findByActivityId(activityId)) {
            User user = findEntityById(participant.getUserId());
            if (user != null && user.getAvatar() != null) {
                avatars.add(user.getAvatar());
            }
        }
        return avatars;
    }
}
