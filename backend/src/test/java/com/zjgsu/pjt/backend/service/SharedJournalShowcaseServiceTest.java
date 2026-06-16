package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.ActivityDiary;
import com.zjgsu.pjt.backend.entity.ActivityDiaryEntry;
import com.zjgsu.pjt.backend.entity.SharedJournalShowcase;
import com.zjgsu.pjt.backend.repository.ActivityDiaryEntryRepository;
import com.zjgsu.pjt.backend.repository.ActivityDiaryRepository;
import com.zjgsu.pjt.backend.repository.SharedJournalShowcaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SharedJournalShowcaseServiceTest {

    @Mock
    private SharedJournalShowcaseRepository showcaseRepository;

    @Mock
    private ActivityDiaryRepository diaryRepository;

    @Mock
    private ActivityDiaryEntryRepository diaryEntryRepository;

    @InjectMocks
    private SharedJournalShowcaseService service;

    @Test
    void shareMyEntry_CreatesShowcaseUsingEntryImageFirst() {
        ActivityDiary diary = diary(10L, "Weekend hiking", "[\"diary-a.png\",\"diary-b.png\"]");
        ActivityDiaryEntry entry = entry(20L, 10L, 7L, "Great view", "[\"entry-a.png\",\"entry-b.png\"]");

        when(diaryRepository.findById(10L)).thenReturn(Optional.of(diary));
        when(diaryEntryRepository.findByDiaryIdAndUserId(10L, 7L)).thenReturn(Optional.of(entry));
        when(showcaseRepository.findByUserIdAndDiaryEntryId(7L, 20L)).thenReturn(Optional.empty());
        when(showcaseRepository.save(any(SharedJournalShowcase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SharedJournalShowcase result = service.shareMyEntry(10L, 7L);

        assertThat(result.getUserId()).isEqualTo(7L);
        assertThat(result.getDiaryId()).isEqualTo(10L);
        assertThat(result.getDiaryEntryId()).isEqualTo(20L);
        assertThat(result.getTitle()).isEqualTo("Weekend hiking");
        assertThat(result.getCoverImage()).isEqualTo("entry-a.png");
        assertThat(result.getExcerpt()).isEqualTo("Great view");
    }

    @Test
    void shareMyEntry_ReusesExistingShowcaseAndFallsBackToDiaryImage() {
        ActivityDiary diary = diary(10L, "Board games", "diary-a.png,diary-b.png");
        ActivityDiaryEntry entry = entry(20L, 10L, 7L, "Fun night", null);
        SharedJournalShowcase existing = new SharedJournalShowcase();
        existing.setId(99L);

        when(diaryRepository.findById(10L)).thenReturn(Optional.of(diary));
        when(diaryEntryRepository.findByDiaryIdAndUserId(10L, 7L)).thenReturn(Optional.of(entry));
        when(showcaseRepository.findByUserIdAndDiaryEntryId(7L, 20L)).thenReturn(Optional.of(existing));
        when(showcaseRepository.save(any(SharedJournalShowcase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SharedJournalShowcase result = service.shareMyEntry(10L, 7L);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getCoverImage()).isEqualTo("diary-a.png");
        assertThat(result.getTitle()).isEqualTo("Board games");
    }

    @Test
    void shareMyEntry_ReturnsNullCoverWhenNoImagesExist() {
        ActivityDiary diary = diary(10L, "No images", null);
        ActivityDiaryEntry entry = entry(20L, 10L, 7L, "Plain text", null);

        when(diaryRepository.findById(10L)).thenReturn(Optional.of(diary));
        when(diaryEntryRepository.findByDiaryIdAndUserId(10L, 7L)).thenReturn(Optional.of(entry));
        when(showcaseRepository.findByUserIdAndDiaryEntryId(7L, 20L)).thenReturn(Optional.empty());
        when(showcaseRepository.save(any(SharedJournalShowcase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SharedJournalShowcase result = service.shareMyEntry(10L, 7L);

        assertThat(result.getCoverImage()).isNull();
    }

    @Test
    void shareMyEntry_RejectsMissingDiaryOrEntry() {
        when(diaryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.shareMyEntry(99L, 7L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Diary not found");

        ActivityDiary diary = diary(10L, "Weekend hiking", null);
        when(diaryRepository.findById(10L)).thenReturn(Optional.of(diary));
        when(diaryEntryRepository.findByDiaryIdAndUserId(10L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.shareMyEntry(10L, 7L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only your own");
    }

    @Test
    void getShowcasesByUserId_MapsRepositoryRecords() {
        SharedJournalShowcase showcase = new SharedJournalShowcase();
        showcase.setDiaryId(10L);
        showcase.setDiaryEntryId(20L);
        showcase.setTitle("Weekend hiking");
        showcase.setCoverImage("cover.png");
        showcase.setExcerpt("Great view");
        when(showcaseRepository.findByUserIdOrderByCreateTimeDesc(7L)).thenReturn(List.of(showcase));

        List<Map<String, Object>> result = service.getShowcasesByUserId(7L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst())
                .containsEntry("id", 10L)
                .containsEntry("sharedEntryId", 20L)
                .containsEntry("title", "Weekend hiking")
                .containsEntry("coverImage", "cover.png")
                .containsEntry("excerpt", "Great view");
    }

    @Test
    void getShowcasesByUserId_ReturnsEmptyList() {
        when(showcaseRepository.findByUserIdOrderByCreateTimeDesc(7L)).thenReturn(List.of());

        List<Map<String, Object>> result = service.getShowcasesByUserId(7L);

        assertThat(result).isEmpty();
    }

    @Test
    void shareMyEntry_PrefersEntryImageOverDiaryImage() {
        ActivityDiary diary = diary(10L, "Weekend hiking", "[\"diary.png\"]");
        ActivityDiaryEntry entry = entry(20L, 10L, 7L, "Great view", "[\"entry.png\"]");

        when(diaryRepository.findById(10L)).thenReturn(Optional.of(diary));
        when(diaryEntryRepository.findByDiaryIdAndUserId(10L, 7L)).thenReturn(Optional.of(entry));
        when(showcaseRepository.findByUserIdAndDiaryEntryId(7L, 20L)).thenReturn(Optional.empty());
        when(showcaseRepository.save(any(SharedJournalShowcase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SharedJournalShowcase result = service.shareMyEntry(10L, 7L);

        assertThat(result.getCoverImage()).isEqualTo("entry.png");
    }

    private ActivityDiary diary(Long id, String title, String images) {
        ActivityDiary diary = new ActivityDiary();
        diary.setId(id);
        diary.setTitle(title);
        diary.setImages(images);
        return diary;
    }

    private ActivityDiaryEntry entry(Long id, Long diaryId, Long userId, String content, String images) {
        ActivityDiaryEntry entry = new ActivityDiaryEntry();
        entry.setId(id);
        entry.setDiaryId(diaryId);
        entry.setUserId(userId);
        entry.setContent(content);
        entry.setImages(images);
        return entry;
    }
}
