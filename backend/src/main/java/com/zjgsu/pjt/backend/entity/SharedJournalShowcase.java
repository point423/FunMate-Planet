package com.zjgsu.pjt.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "shared_journal_showcase")
@Data
public class SharedJournalShowcase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "diary_id", nullable = false)
    private Long diaryId;

    @Column(name = "diary_entry_id", nullable = false)
    private Long diaryEntryId;

    @Column(name = "title")
    private String title;

    @Column(name = "cover_image", columnDefinition = "TEXT")
    private String coverImage;

    @Column(name = "excerpt", columnDefinition = "TEXT")
    private String excerpt;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();
}
