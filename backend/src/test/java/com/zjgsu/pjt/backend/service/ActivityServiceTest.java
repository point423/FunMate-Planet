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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivityParticipantRepository participantRepository;

    @Mock
    private ActivityDiaryRepository diaryRepository;

    @Mock
    private ActivityInvitationService activityInvitationService;

    @InjectMocks
    private ActivityService activityService;

    @Test
    @DisplayName("非创建者修改活动时返回 null")
    void updateActivity_Forbidden_ReturnsNull() {
        Activity existing = new Activity();
        existing.setId(1L);
        existing.setCreatorId(99L);

        when(activityRepository.findById(1L)).thenReturn(Optional.of(existing));

        Activity result = activityService.updateActivity(1L, new Activity(), 1L);
        assertNull(result);
    }

    @Test
    @DisplayName("编辑活动未显式传 status 时保留原状态")
    void updateActivity_WithoutStatus_KeepsExistingStatus() {
        Activity existing = new Activity();
        existing.setId(1L);
        existing.setCreatorId(1L);
        existing.setStatus(1);
        existing.setTitle("Before");

        Activity update = new Activity();
        update.setTitle("After");

        when(activityRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Activity result = activityService.updateActivity(1L, update, 1L);

        assertNotNull(result);
        assertEquals(1, result.getStatus());
        assertEquals("After", result.getTitle());
    }

    @Test
    @DisplayName("非创建者删除活动时返回 false")
    void deleteActivity_Forbidden_ReturnsFalse() {
        Activity existing = new Activity();
        existing.setId(1L);
        existing.setCreatorId(99L);

        when(activityRepository.findById(1L)).thenReturn(Optional.of(existing));

        boolean result = activityService.deleteActivity(1L, 1L);
        assertFalse(result);
    }

    @Test
    @DisplayName("非创建者结束活动时返回 false")
    void endActivity_Forbidden_ReturnsFalse() {
        Activity existing = new Activity();
        existing.setId(1L);
        existing.setCreatorId(99L);
        when(activityRepository.findById(1L)).thenReturn(Optional.of(existing));

        boolean result = activityService.endActivity(1L, 1L);
        assertFalse(result);
    }

    @Test
    @DisplayName("创建活动成功并自动加入创建者")
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
    @DisplayName("创建活动时带 inviteeId 会额外创建邀请")
    void createActivity_WithInvitee_CreatesInvitation() {
        Activity activity = new Activity();
        activity.setCreatorId(1L);
        activity.setInviteeId(2L);

        Activity saved = new Activity();
        saved.setId(10L);
        saved.setCreatorId(1L);

        when(activityRepository.save(any())).thenReturn(saved);
        when(participantRepository.findByActivityIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        activityService.createActivity(activity);

        verify(activityInvitationService).createInvitation(10L, 1L, 2L);
    }

    @Test
    @DisplayName("我的活动列表会分组参与和自己创建的 pending 活动")
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
    @DisplayName("活动有日记时返回 true")
    void hasJournal_ReturnsTrueWhenDiaryExists() {
        when(diaryRepository.existsByActivityId(10L)).thenReturn(true);
        assertTrue(activityService.hasJournal(10L));
    }

    @Test
    @DisplayName("创建者可对已有活动再次邀请好友")
    void inviteFriend_CreatorCanInvite() {
        Activity activity = activity(10L, 1L, 0);
        when(activityRepository.findById(10L)).thenReturn(Optional.of(activity));

        Activity result = activityService.inviteFriend(10L, 1L, 2L);

        assertEquals(10L, result.getId());
        verify(activityInvitationService).createInvitation(10L, 1L, 2L);
    }

    @Test
    @DisplayName("非创建者不能对活动发送邀请")
    void inviteFriend_NonCreatorThrows() {
        Activity activity = activity(10L, 99L, 0);
        when(activityRepository.findById(10L)).thenReturn(Optional.of(activity));

        assertThrows(IllegalArgumentException.class, () -> activityService.inviteFriend(10L, 1L, 2L));
    }

    private Activity activity(Long id, Long creatorId, Integer status) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setCreatorId(creatorId);
        activity.setStatus(status);
        return activity;
    }
}
