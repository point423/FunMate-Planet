package com.zjgsu.pjt.backend.repository;

import com.zjgsu.pjt.backend.entity.ActivityDiaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityDiaryEntryRepository extends JpaRepository<ActivityDiaryEntry, Long> {
    List<ActivityDiaryEntry> findByDiaryId(Long diaryId);
    Optional<ActivityDiaryEntry> findByDiaryIdAndUserId(Long diaryId, Long userId);
}
