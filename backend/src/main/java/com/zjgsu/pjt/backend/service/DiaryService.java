package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.ActivityDiary;
import com.zjgsu.pjt.backend.entity.ActivityDiaryEntry;
import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.entity.ActivityParticipant;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.ActivityRepository;
import com.zjgsu.pjt.backend.repository.ActivityDiaryEntryRepository;
import com.zjgsu.pjt.backend.repository.ActivityDiaryRepository;
import com.zjgsu.pjt.backend.repository.ActivityParticipantRepository;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityDiaryEntryRepository diaryEntryRepository;

    @Autowired
    private UserRepository userRepository;

    public ActivityDiary createDiary(ActivityDiary diary) {
        ActivityDiary saved = diaryRepository.save(diary);
        bootstrapDiaryEntries(saved);
        return saved;
    }

    public Page<ActivityDiary> getDiaries(Long userId, Pageable pageable) {
        if (userId != null) {
            return diaryRepository.findRelevantByUserId(userId, pageable);
        }
        return Page.empty(pageable);
    }

    public Page<Map<String, Object>> getDiariesWithParticipants(Long userId, Pageable pageable) {
        Page<ActivityDiary> diaryPage = userId != null
                ? diaryRepository.findRelevantByUserId(userId, pageable)
                : Page.empty(pageable);

        List<Map<String, Object>> contentWithParticipants = diaryPage.getContent().stream().map(diary -> {
            Map<String, Object> map = new HashMap<>();
            map.put("diary", diary);
            map.put("participants", getParticipantsByDiaryId(diary.getId()));
            return map;
        }).collect(Collectors.toList());

        return new PageImpl<>(contentWithParticipants, pageable, diaryPage.getTotalElements());
    }

    public ActivityDiary findById(Long id) {
        return diaryRepository.findById(id).orElse(null);
    }

    public boolean canAccessDiary(Long diaryId, Long currentUserId) {
        ActivityDiary diary = findById(diaryId);
        if (diary == null || currentUserId == null) {
            return false;
        }

        if (Objects.equals(diary.getUserId(), currentUserId)) {
            return true;
        }

        if (diary.getActivityId() == null) {
            return false;
        }

        return activityParticipantRepository.findByActivityId(diary.getActivityId()).stream()
                .anyMatch(participant -> Objects.equals(participant.getUserId(), currentUserId));
    }

    public ActivityDiary updateDiary(Long id, ActivityDiary updated, Long currentUserId) {
        ActivityDiary existing = findById(id);
        if (existing != null) {
            if (!Objects.equals(existing.getUserId(), currentUserId)) {
                return null;
            }
            if (updated.getContent() != null) existing.setContent(updated.getContent());
            if (updated.getImages() != null) existing.setImages(updated.getImages());
            if (updated.getTags() != null) existing.setTags(updated.getTags());
            ActivityDiary saved = diaryRepository.save(existing);
            bootstrapDiaryEntries(saved);
            return saved;
        }
        return null;
    }

    public boolean deleteDiary(Long id, Long currentUserId) {
        ActivityDiary existing = findById(id);
        if (existing != null) {
            boolean isDiaryOwner = Objects.equals(existing.getUserId(), currentUserId);
            boolean isActivityCreator = false;
            if (existing.getActivityId() != null) {
                Activity activity = activityRepository.findById(existing.getActivityId()).orElse(null);
                isActivityCreator = activity != null && Objects.equals(activity.getCreatorId(), currentUserId);
            }

            if (!isDiaryOwner && !isActivityCreator) {
                return false;
            }
            diaryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public ActivityDiary createDiaryWithParticipants(ActivityDiary diary, List<Long> participantIds) {
        ActivityDiary savedDiary = diaryRepository.save(diary);

        if (participantIds != null && !participantIds.isEmpty()) {
            for (Long userId : participantIds) {
                ActivityParticipant participant = new ActivityParticipant();
                participant.setActivityId(savedDiary.getActivityId());
                participant.setUserId(userId);
                participant.setStatus(1);
                activityParticipantRepository.save(participant);
            }
        }

        bootstrapDiaryEntries(savedDiary);
        return savedDiary;
    }

    public List<User> getParticipantsByDiaryId(Long diaryId) {
        ActivityDiary diary = findById(diaryId);
        if (diary == null || diary.getActivityId() == null) {
            return List.of();
        }

        List<ActivityParticipant> participants = activityParticipantRepository.findByActivityId(diary.getActivityId());
        return participants.stream()
                .map(participant -> userRepository.findById(participant.getUserId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getSharedEntriesByDiaryId(Long diaryId) {
        ActivityDiary diary = findById(diaryId);
        if (diary == null) {
            return List.of();
        }

        bootstrapDiaryEntries(diary);
        List<ActivityDiaryEntry> entries = diaryEntryRepository.findByDiaryId(diaryId);
        List<User> participants = getParticipantsByDiaryId(diaryId);

        return participants.stream().map(user -> {
            ActivityDiaryEntry entry = entries.stream()
                    .filter(candidate -> Objects.equals(candidate.getUserId(), user.getId()))
                    .findFirst()
                    .orElseGet(() -> {
                        ActivityDiaryEntry created = new ActivityDiaryEntry();
                        created.setDiaryId(diaryId);
                        created.setUserId(user.getId());
                        return diaryEntryRepository.save(created);
                    });

            Map<String, Object> item = new HashMap<>();
            item.put("user", user);
            item.put("entry", entry);
            return item;
        }).collect(Collectors.toList());
    }

    public ActivityDiaryEntry updateSharedEntry(Long diaryId, Long currentUserId, ActivityDiaryEntry updated) {
        ActivityDiary diary = findById(diaryId);
        if (diary == null) {
            return null;
        }

        ActivityDiaryEntry entry = diaryEntryRepository.findByDiaryIdAndUserId(diaryId, currentUserId)
                .orElseGet(() -> {
                    ActivityDiaryEntry created = new ActivityDiaryEntry();
                    created.setDiaryId(diaryId);
                    created.setUserId(currentUserId);
                    return created;
                });

        if (updated.getContent() != null) entry.setContent(updated.getContent());
        if (updated.getImages() != null) entry.setImages(updated.getImages());
        entry.setUpdateTime(LocalDateTime.now());
        return diaryEntryRepository.save(entry);
    }

    private void bootstrapDiaryEntries(ActivityDiary diary) {
        if (diary == null || diary.getId() == null) return;

        List<User> participants = getParticipantsByDiaryId(diary.getId());
        if (participants.isEmpty() && diary.getUserId() != null) {
            User author = userRepository.findById(diary.getUserId()).orElse(null);
            if (author != null) {
                participants = List.of(author);
            }
        }

        for (User participant : participants) {
            if (participant == null || participant.getId() == null) continue;
            diaryEntryRepository.findByDiaryIdAndUserId(diary.getId(), participant.getId())
                    .orElseGet(() -> {
                        ActivityDiaryEntry entry = new ActivityDiaryEntry();
                        entry.setDiaryId(diary.getId());
                        entry.setUserId(participant.getId());
                        if (Objects.equals(participant.getId(), diary.getUserId())) {
                            entry.setContent(diary.getContent());
                            entry.setImages(diary.getImages());
                        }
                        return diaryEntryRepository.save(entry);
                    });
        }
    }
}
