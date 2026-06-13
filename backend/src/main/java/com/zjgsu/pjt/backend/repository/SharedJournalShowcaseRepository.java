package com.zjgsu.pjt.backend.repository;

import com.zjgsu.pjt.backend.entity.SharedJournalShowcase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharedJournalShowcaseRepository extends JpaRepository<SharedJournalShowcase, Long> {
    List<SharedJournalShowcase> findByUserIdOrderByCreateTimeDesc(Long userId);
    Optional<SharedJournalShowcase> findByUserIdAndDiaryEntryId(Long userId, Long diaryEntryId);
}
