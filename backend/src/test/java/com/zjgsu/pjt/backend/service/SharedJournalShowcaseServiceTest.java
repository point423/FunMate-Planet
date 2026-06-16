package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.ActivityDiary;
import com.zjgsu.pjt.backend.entity.ActivityDiaryEntry;
import com.zjgsu.pjt.backend.entity.SharedJournalShowcase;
import com.zjgsu.pjt.backend.repository.ActivityDiaryEntryRepository;
import com.zjgsu.pjt.backend.repository.ActivityDiaryRepository;
import com.zjgsu.pjt.backend.repository.SharedJournalShowcaseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SharedJournalShowcaseServiceTest {

    @Mock
    private SharedJournalShowcaseRepository showcaseRepository;

    @Mock
    private ActivityDiaryRepository diaryRepository;

    @Mock
    private ActivityDiaryEntryRepository diaryEntryRepository;

    @InjectMocks
    private SharedJournalShowcaseService service;

    private ActivityDiary makeDiary(Long id, String title, String images) {
        ActivityDiary d = new ActivityDiary();
        d.setId(id);
        d.setTitle(title);
        d.setImages(images);
        return d;
    }

    private ActivityDiaryEntry makeEntry(Long id, Long diaryId, Long userId, String content, String images) {
        ActivityDiaryEntry e = new ActivityDiaryEntry();
        e.setId(id);
        e.setDiaryId(diaryId);
        e.setUserId(userId);
        e.setContent(content);
        e.setImages(images);
        return e;
    }

    @Test
    @DisplayName("分享我的日记-成功(新记录)")
    void shareMyEntry_NewRecord() {
        ActivityDiary diary = makeDiary(1L, "西湖徒步", "[\"a.jpg\",\"b.jpg\"]");
        ActivityDiaryEntry entry = makeEntry(10L, 1L, 100L, "今天去了西湖", "[\"c.jpg\"]");

        when(diaryRepository.findById(1L)).thenReturn(Optional.of(diary));
        when(diaryEntryRepository.findByDiaryIdAndUserId(1L, 100L)).thenReturn(Optional.of(entry));
        when(showcaseRepository.findByUserIdAndDiaryEntryId(100L, 10L)).thenReturn(Optional.empty());
        when(showcaseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SharedJournalShowcase result = service.shareMyEntry(1L, 100L);

        assertNotNull(result);
        assertEquals(100L, result.getUserId());
        assertEquals(1L, result.getDiaryId());
        assertEquals(10L, result.getDiaryEntryId());
        assertEquals("西湖徒步", result.getTitle());
        assertEquals("今天去了西湖", result.getExcerpt());
        // entry 有图,优先用 entry 的
        assertEquals("c.jpg", result.getCoverImage());
    }

    @Test
    @DisplayName("分享我的日记-成功(更新已有记录)")
    void shareMyEntry_UpdateExisting() {
        ActivityDiary diary = makeDiary(1L, "西湖徒步", null);
        ActivityDiaryEntry entry = makeEntry(10L, 1L, 100L, "今天", null);
        SharedJournalShowcase existing = new SharedJournalShowcase();
        existing.setId(99L);

        when(diaryRepository.findById(1L)).thenReturn(Optional.of(diary));
        when(diaryEntryRepository.findByDiaryIdAndUserId(1L, 100L)).thenReturn(Optional.of(entry));
        when(showcaseRepository.findByUserIdAndDiaryEntryId(100L, 10L)).thenReturn(Optional.of(existing));
        when(showcaseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SharedJournalShowcase result = service.shareMyEntry(1L, 100L);
        assertNotNull(result);
        // entry 和 diary 都没图,封面为 null
        assertNull(result.getCoverImage());
    }

    @Test
    @DisplayName("分享我的日记-日记不存在抛异常")
    void shareMyEntry_DiaryNotFound_Throws() {
        when(diaryRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.shareMyEntry(999L, 100L)
        );
        assertTrue(ex.getMessage().contains("Diary not found"));
    }

    @Test
    @DisplayName("分享我的日记-只能分享自己的卡片")
    void shareMyEntry_NotYourEntry_Throws() {
        ActivityDiary diary = makeDiary(1L, "西湖徒步", null);
        when(diaryRepository.findById(1L)).thenReturn(Optional.of(diary));
        // 找不到当前用户对应的 entry
        when(diaryEntryRepository.findByDiaryIdAndUserId(1L, 100L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.shareMyEntry(1L, 100L)
        );
        assertTrue(ex.getMessage().contains("Only your own"));
    }

    @Test
    @DisplayName("查某用户的所有分享-返回Map列表")
    void getShowcasesByUserId_ReturnsList() {
        SharedJournalShowcase s1 = new SharedJournalShowcase();
        s1.setDiaryId(1L);
        s1.setTitle("徒步");
        s1.setCoverImage("a.jpg");
        s1.setExcerpt("excerpt 1");
        s1.setDiaryEntryId(10L);

        SharedJournalShowcase s2 = new SharedJournalShowcase();
        s2.setDiaryId(2L);
        s2.setTitle("美食");
        s2.setCoverImage("b.jpg");
        s2.setExcerpt("excerpt 2");
        s2.setDiaryEntryId(11L);

        when(showcaseRepository.findByUserIdOrderByCreateTimeDesc(100L))
                .thenReturn(Arrays.asList(s1, s2));

        List<Map<String, Object>> result = service.getShowcasesByUserId(100L);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).get("id"));
        assertEquals("a.jpg", result.get(0).get("coverImage"));
        assertEquals("徒步", result.get(0).get("title"));
    }

    @Test
    @DisplayName("查某用户的分享-空列表")
    void getShowcasesByUserId_Empty() {
        when(showcaseRepository.findByUserIdOrderByCreateTimeDesc(999L))
                .thenReturn(Arrays.asList());

        List<Map<String, Object>> result = service.getShowcasesByUserId(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("封面图-优先用 entry 的图")
    void coverImage_PrefersEntry() {
        ActivityDiary diary = makeDiary(1L, "title", "[\"diary.jpg\"]");
        ActivityDiaryEntry entry = makeEntry(10L, 1L, 100L, "c", "[\"entry.jpg\"]");

        when(diaryRepository.findById(1L)).thenReturn(Optional.of(diary));
        when(diaryEntryRepository.findByDiaryIdAndUserId(1L, 100L)).thenReturn(Optional.of(entry));
        when(showcaseRepository.findByUserIdAndDiaryEntryId(any(), any())).thenReturn(Optional.empty());
        when(showcaseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SharedJournalShowcase result = service.shareMyEntry(1L, 100L);
        assertEquals("entry.jpg", result.getCoverImage());
    }

    @Test
    @DisplayName("封面图-entry 无图时用 diary 的图")
    void coverImage_FallbackToDiary() {
        ActivityDiary diary = makeDiary(1L, "title", "diary.jpg");
        ActivityDiaryEntry entry = makeEntry(10L, 1L, 100L, "c", null);

        when(diaryRepository.findById(1L)).thenReturn(Optional.of(diary));
        when(diaryEntryRepository.findByDiaryIdAndUserId(1L, 100L)).thenReturn(Optional.of(entry));
        when(showcaseRepository.findByUserIdAndDiaryEntryId(any(), any())).thenReturn(Optional.empty());
        when(showcaseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SharedJournalShowcase result = service.shareMyEntry(1L, 100L);
        assertEquals("diary.jpg", result.getCoverImage());
    }
}