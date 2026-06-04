package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.ActivityDiary;
import com.zjgsu.pjt.backend.repository.ActivityDiaryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DiaryServiceTest {

    @Mock
    private ActivityDiaryRepository diaryRepository;

    @InjectMocks
    private DiaryService diaryService;

    @Test
    @DisplayName("测试创建日记-成功")
    void createDiary_Success() {
        ActivityDiary diary = new ActivityDiary();
        diary.setContent("测试日记");
        when(diaryRepository.save(any())).thenReturn(diary);

        ActivityDiary result = diaryService.createDiary(diary);
        assertEquals("测试日记", result.getContent());
    }

    @Test
    @DisplayName("测试根据用户查询日记")
    void getDiaries_ByUser() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<ActivityDiary> page = new PageImpl<>(Collections.singletonList(new ActivityDiary()));
        when(diaryRepository.findByUserId(eq(1L), any())).thenReturn(page);

        // ✅ 修复：传入 Pageable 参数
        Page<ActivityDiary> result = diaryService.getDiaries(1L, pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("测试更新日记逻辑")
    void updateDiary_Success() {
        ActivityDiary existing = new ActivityDiary();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setContent("旧内容");

        ActivityDiary updateInfo = new ActivityDiary();
        updateInfo.setContent("新内容");

        when(diaryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(diaryRepository.save(any())).thenReturn(existing);

        // ✅ 修复：传入 currentUserId 参数 (1L)
        ActivityDiary result = diaryService.updateDiary(1L, updateInfo, 1L);
        assertEquals("新内容", result.getContent());
    }
}
