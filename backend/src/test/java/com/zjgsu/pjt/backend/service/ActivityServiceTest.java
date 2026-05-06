package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.repository.ActivityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

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
        when(activityRepository.save(any())).thenReturn(activity);
        assertNotNull(activityService.createActivity(activity));
    }
}
