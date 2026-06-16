package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.entity.ActivityInvitation;
import com.zjgsu.pjt.backend.entity.ActivityParticipant;
import com.zjgsu.pjt.backend.entity.Friendship;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.ActivityInvitationRepository;
import com.zjgsu.pjt.backend.repository.ActivityParticipantRepository;
import com.zjgsu.pjt.backend.repository.ActivityRepository;
import com.zjgsu.pjt.backend.repository.FriendshipRepository;
import com.zjgsu.pjt.backend.repository.UserRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ActivityInvitationServiceTest {

    @Mock
    private ActivityInvitationRepository invitationRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivityParticipantRepository participantRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ActivityInvitationService activityInvitationService;

    @Test
    @DisplayName("创建邀请成功")
    void createInvitation_Success() {
        Activity activity = new Activity();
        activity.setId(10L);
        activity.setCreatorId(1L);

        User invitee = new User();
        invitee.setId(2L);

        when(activityRepository.findById(10L)).thenReturn(Optional.of(activity));
        when(userRepository.findById(2L)).thenReturn(Optional.of(invitee));
        when(friendshipRepository.findAllByUserIdAndFriendId(1L, 2L)).thenReturn(List.of(new Friendship()));
        when(friendshipRepository.findAllByUserIdAndFriendId(2L, 1L)).thenReturn(List.of(new Friendship()));
        when(participantRepository.findByActivityIdAndUserId(10L, 2L)).thenReturn(Optional.empty());
        when(invitationRepository.findByActivityIdAndReceiverIdAndStatus(10L, 2L, "pending")).thenReturn(Optional.empty());
        when(invitationRepository.save(any(ActivityInvitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActivityInvitation result = activityInvitationService.createInvitation(10L, 1L, 2L);

        assertEquals(10L, result.getActivityId());
        assertEquals(1L, result.getSenderId());
        assertEquals(2L, result.getReceiverId());
        assertEquals("pending", result.getStatus());
    }

    @Test
    @DisplayName("接受邀请后成为参与者并推动活动进入 active")
    void handleInvitation_Accepted_JoinsParticipantAndActivatesActivity() {
        Activity activity = new Activity();
        activity.setId(10L);
        activity.setCreatorId(1L);
        activity.setStatus(0);
        activity.setMaxParticipants(4);

        ActivityInvitation invitation = new ActivityInvitation();
        invitation.setId(100L);
        invitation.setActivityId(10L);
        invitation.setSenderId(1L);
        invitation.setReceiverId(2L);
        invitation.setStatus("pending");

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(activityRepository.findById(10L)).thenReturn(Optional.of(activity));
        when(participantRepository.findByActivityId(10L)).thenReturn(List.of());
        when(participantRepository.findByActivityIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

        boolean result = activityInvitationService.handleInvitation(100L, true, 2L);

        assertTrue(result);
        assertEquals("accepted", invitation.getStatus());
        assertEquals(1, activity.getStatus());
        verify(participantRepository).save(any(ActivityParticipant.class));
        verify(activityRepository).save(activity);
    }

    @Test
    @DisplayName("接收人可以看到自己的邀请列表")
    void getMyInvitations_ReturnsIncomingAndOutgoing() {
        ActivityInvitation incoming = new ActivityInvitation();
        incoming.setId(1L);
        incoming.setActivityId(10L);
        incoming.setSenderId(2L);
        incoming.setReceiverId(1L);

        ActivityInvitation outgoing = new ActivityInvitation();
        outgoing.setId(2L);
        outgoing.setActivityId(20L);
        outgoing.setSenderId(1L);
        outgoing.setReceiverId(3L);

        when(invitationRepository.findByReceiverIdOrderByCreateTimeDesc(1L)).thenReturn(List.of(incoming));
        when(invitationRepository.findBySenderIdOrderByCreateTimeDesc(1L)).thenReturn(List.of(outgoing));
        when(activityRepository.findById(10L)).thenReturn(Optional.of(new Activity()));
        when(activityRepository.findById(20L)).thenReturn(Optional.of(new Activity()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(3L)).thenReturn(Optional.of(new User()));

        Map<String, Object> result = activityInvitationService.getMyInvitations(1L);

        assertTrue(result.containsKey("incoming"));
        assertTrue(result.containsKey("outgoing"));
    }
}
