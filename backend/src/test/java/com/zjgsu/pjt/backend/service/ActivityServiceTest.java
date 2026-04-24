package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.repository.ActivityRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ActivityService activityService;

    @Test
    @DisplayName("测试创建活动")
    void createActivity_Success() {
        Activity activity = new Activity();
        activity.setTitle("测试活动");
        when(activityRepository.save(any())).thenReturn(activity);

        Activity result = activityService.createActivity(activity);
        assertEquals("测试活动", result.getTitle());
    }

    @Test
    @DisplayName("测试分页查询活动")
    void getActivities_ReturnsPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Activity> page = new PageImpl<>(Collections.singletonList(new Activity()));
        when(activityRepository.findAll(pageable)).thenReturn(page);

        Page<Activity> result = activityService.getActivities(null, pageable);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("测试根据ID查询活动")
    void findById_Exists() {
        Activity activity = new Activity();
        activity.setId(1L);
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));

        Activity result = activityService.findById(1L);
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试更新活动逻辑")
    void updateActivity_Success() {
        Activity existing = new Activity();
        existing.setId(1L);
        existing.setTitle("旧标题");

        Activity updateInfo = new Activity();
        updateInfo.setTitle("新标题");

        when(activityRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(activityRepository.save(any())).thenReturn(existing);

        Activity result = activityService.updateActivity(1L, updateInfo);
        assertEquals("新标题", result.getTitle());
    }

    @Test
    @DisplayName("测试删除活动")
    void deleteActivity_Success() {
        when(activityRepository.existsById(1L)).thenReturn(true);
        boolean result = activityService.deleteActivity(1L);
        assertTrue(result);
        verify(activityRepository).deleteById(1L);
    }
}
