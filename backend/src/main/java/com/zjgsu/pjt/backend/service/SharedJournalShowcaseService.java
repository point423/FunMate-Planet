package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.ActivityDiary;
import com.zjgsu.pjt.backend.entity.ActivityDiaryEntry;
import com.zjgsu.pjt.backend.entity.SharedJournalShowcase;
import com.zjgsu.pjt.backend.repository.ActivityDiaryEntryRepository;
import com.zjgsu.pjt.backend.repository.ActivityDiaryRepository;
import com.zjgsu.pjt.backend.repository.SharedJournalShowcaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SharedJournalShowcaseService {

    @Autowired
    private SharedJournalShowcaseRepository showcaseRepository;

    @Autowired
    private ActivityDiaryRepository diaryRepository;

    @Autowired
    private ActivityDiaryEntryRepository diaryEntryRepository;

    @Transactional
    public SharedJournalShowcase shareMyEntry(Long diaryId, Long currentUserId) {
        ActivityDiary diary = diaryRepository.findById(diaryId).orElse(null);
        if (diary == null) {
            throw new IllegalArgumentException("Diary not found");
        }

        ActivityDiaryEntry entry = diaryEntryRepository.findByDiaryIdAndUserId(diaryId, currentUserId).orElse(null);
        if (entry == null) {
            throw new IllegalArgumentException("Only your own shared card can be shared");
        }

        SharedJournalShowcase showcase = showcaseRepository
                .findByUserIdAndDiaryEntryId(currentUserId, entry.getId())
                .orElseGet(SharedJournalShowcase::new);

        showcase.setUserId(currentUserId);
        showcase.setDiaryId(diaryId);
        showcase.setDiaryEntryId(entry.getId());
        showcase.setTitle(diary.getTitle());
        showcase.setCoverImage(resolveCoverImage(entry, diary));
        showcase.setExcerpt(entry.getContent());
        return showcaseRepository.save(showcase);
    }

    public List<Map<String, Object>> getShowcasesByUserId(Long userId) {
        return showcaseRepository.findByUserIdOrderByCreateTimeDesc(userId).stream()
                .map(showcase -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", showcase.getDiaryId());
                    item.put("coverImage", showcase.getCoverImage());
                    item.put("title", showcase.getTitle());
                    item.put("excerpt", showcase.getExcerpt());
                    item.put("sharedEntryId", showcase.getDiaryEntryId());
                    return item;
                })
                .collect(Collectors.toList());
    }

    private String resolveCoverImage(ActivityDiaryEntry entry, ActivityDiary diary) {
        String entryImages = entry.getImages();
        if (entryImages != null && !entryImages.isBlank()) {
            return firstImage(entryImages);
        }
        String diaryImages = diary.getImages();
        if (diaryImages != null && !diaryImages.isBlank()) {
            return firstImage(diaryImages);
        }
        return null;
    }

    private String firstImage(String raw) {
        String value = raw.trim();
        if (value.startsWith("[") && value.endsWith("]")) {
            String body = value.substring(1, value.length() - 1).trim();
            if (body.startsWith("\"")) {
                String[] parts = body.split(",");
                if (parts.length > 0) {
                    return parts[0].replace("\"", "").trim();
                }
            }
        }
        if (value.contains(",")) {
            return value.split(",")[0].replace("\"", "").trim();
        }
        return value.replace("\"", "").trim();
    }
}
