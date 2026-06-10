package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.entity.ActivityParticipant;
import com.zjgsu.pjt.backend.repository.ActivityDiaryRepository;
import com.zjgsu.pjt.backend.repository.ActivityParticipantRepository;
import com.zjgsu.pjt.backend.repository.ActivityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivityParticipantRepository participantRepository;

    @Mock
    private ActivityDiaryRepository diaryRepository;

    @InjectMocks
    private ActivityService activityService;

    @Test
    @DisplayName("安全校验-非创建者修改活动应返回null")
    void updateActivity_Forbidden_ReturnsNull() {
        Activity existing = new Activity();
        existing.setId(1L);
        existing.setCreatorId(99L); // 创建者是 99

        when(activityRepository.findById(1L)).thenReturn(Optional.of(existing));

        // 当前登录用户是 1L，尝试修改 99L 的活动
        Activity result = activityService.updateActivity(1L, new Activity(), 1L);
        assertNull(result);
    }

    @Test
    @DisplayName("安全校验-非创建者删除活动应返回false")
    void deleteActivity_Forbidden_ReturnsFalse() {
        Activity existing = new Activity();
        existing.setId(1L);
        existing.setCreatorId(99L);

        when(activityRepository.findById(1L)).thenReturn(Optional.of(existing));

        boolean result = activityService.deleteActivity(1L, 1L);
        assertFalse(result);
    }

    @Test
    @DisplayName("安全校验-非创建者结束活动应返回false")
    void endActivity_Forbidden_ReturnsFalse() {
        Activity existing = new Activity();
        existing.setId(1L);
        existing.setCreatorId(99L);
        when(activityRepository.findById(1L)).thenReturn(Optional.of(existing));

        boolean result = activityService.endActivity(1L, 1L);
        assertFalse(result);
    }

    @Test
    @DisplayName("创建活动成功")
    void createActivity_Success() {
        Activity activity = new Activity();
        activity.setCreatorId(1L);

        Activity saved = new Activity();
        saved.setId(10L);
        saved.setCreatorId(1L);

        when(activityRepository.save(any())).thenReturn(saved);
        when(participantRepository.findByActivityIdAndUserId(10L, 1L)).thenReturn(Optional.empty());
        when(participantRepository.save(any(ActivityParticipant.class))).thenReturn(new ActivityParticipant());

        assertNotNull(activityService.createActivity(activity));
    }

    @Test
    @DisplayName("鎴戠殑娲诲姩-鎸夌姸鎬佸垎缁勫苟鍖呭惈鍒涘缓鑰卲ending")
    void getMyActivities_GroupsParticipatedAndCreatorPending() {
        ActivityParticipant activeParticipant = new ActivityParticipant();
        activeParticipant.setActivityId(10L);
        ActivityParticipant completedParticipant = new ActivityParticipant();
        completedParticipant.setActivityId(20L);

        Activity active = activity(10L, 2L, 1);
        Activity completed = activity(20L, 3L, 2);
        Activity pendingByCreator = activity(30L, 1L, 0);

        when(participantRepository.findByUserId(1L)).thenReturn(List.of(activeParticipant, completedParticipant));
        when(activityRepository.findByIdIn(anyCollection())).thenReturn(List.of(active, completed));
        when(activityRepository.findByCreatorIdAndStatus(1L, 0)).thenReturn(List.of(pendingByCreator));

        Map<String, List<Activity>> result = activityService.getMyActivities(1L);

        assertEquals(List.of(pendingByCreator), result.get("pending"));
        assertEquals(List.of(active), result.get("active"));
        assertEquals(List.of(completed), result.get("completed"));
        assertTrue(result.get("cancelled").isEmpty());
    }

    @Test
    @DisplayName("娲诲姩璇︽儏-鍒ゆ柇鏄惁宸叉湁鏃ヨ")
    void hasJournal_ReturnsTrueWhenDiaryExists() {
        when(diaryRepository.existsByActivityId(10L)).thenReturn(true);

        assertTrue(activityService.hasJournal(10L));
    }

    private Activity activity(Long id, Long creatorId, Integer status) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setCreatorId(creatorId);
        activity.setStatus(status);
        return activity;
    }
}
