package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.entity.ActivityDiary;
import com.zjgsu.pjt.backend.repository.ActivityDiaryEntryRepository;
import com.zjgsu.pjt.backend.repository.ActivityDiaryRepository;
import com.zjgsu.pjt.backend.repository.ActivityParticipantRepository;
import com.zjgsu.pjt.backend.repository.ActivityRepository;
import com.zjgsu.pjt.backend.repository.UserRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DiaryServiceTest {

    @Mock
    private ActivityDiaryRepository diaryRepository;

    @Mock
    private ActivityParticipantRepository activityParticipantRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivityDiaryEntryRepository diaryEntryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DiaryService diaryService;

    @Test
    @DisplayName("创建日记成功")
    void createDiary_Success() {
        ActivityDiary diary = new ActivityDiary();
        diary.setContent("测试日记");
        when(diaryRepository.save(any())).thenReturn(diary);

        ActivityDiary result = diaryService.createDiary(diary);
        assertEquals("测试日记", result.getContent());
    }

    @Test
    @DisplayName("根据用户查询相关日记")
    void getDiaries_ByUser() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<ActivityDiary> page = new PageImpl<>(Collections.singletonList(new ActivityDiary()));
        when(diaryRepository.findRelevantByUserId(eq(1L), any())).thenReturn(page);

        Page<ActivityDiary> result = diaryService.getDiaries(1L, pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("更新日记成功")
    void updateDiary_Success() {
        ActivityDiary existing = new ActivityDiary();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setContent("旧内容");

        ActivityDiary updateInfo = new ActivityDiary();
        updateInfo.setContent("新内容");

        when(diaryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(diaryRepository.save(any())).thenReturn(existing);

        ActivityDiary result = diaryService.updateDiary(1L, updateInfo, 1L);
        assertEquals("新内容", result.getContent());
    }

    @Test
    @DisplayName("日记作者可以删除日记")
    void deleteDiary_DiaryOwner_Success() {
        ActivityDiary existing = new ActivityDiary();
        existing.setId(1L);
        existing.setUserId(8L);

        when(diaryRepository.findById(1L)).thenReturn(Optional.of(existing));

        boolean result = diaryService.deleteDiary(1L, 8L);
        assertTrue(result);
    }

    @Test
    @DisplayName("活动发起者也可以删除历史错绑作者的共享日记")
    void deleteDiary_ActivityCreator_Success() {
        ActivityDiary existing = new ActivityDiary();
        existing.setId(2L);
        existing.setUserId(99L);
        existing.setActivityId(10L);

        Activity activity = new Activity();
        activity.setId(10L);
        activity.setCreatorId(8L);

        when(diaryRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(activityRepository.findById(10L)).thenReturn(Optional.of(activity));

        boolean result = diaryService.deleteDiary(2L, 8L);
        assertTrue(result);
    }

    @Test
    @DisplayName("无关用户不能删除共享日记")
    void deleteDiary_UnrelatedUser_Forbidden() {
        ActivityDiary existing = new ActivityDiary();
        existing.setId(3L);
        existing.setUserId(99L);
        existing.setActivityId(10L);

        Activity activity = new Activity();
        activity.setId(10L);
        activity.setCreatorId(77L);

        when(diaryRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(activityRepository.findById(10L)).thenReturn(Optional.of(activity));

        boolean result = diaryService.deleteDiary(3L, 8L);
        assertFalse(result);
    }
}
