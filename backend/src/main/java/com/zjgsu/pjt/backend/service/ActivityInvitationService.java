package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.entity.ActivityInvitation;
import com.zjgsu.pjt.backend.entity.ActivityParticipant;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.ActivityInvitationRepository;
import com.zjgsu.pjt.backend.repository.ActivityParticipantRepository;
import com.zjgsu.pjt.backend.repository.ActivityRepository;
import com.zjgsu.pjt.backend.repository.FriendshipRepository;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ActivityInvitationService {

    @Autowired
    private ActivityInvitationRepository invitationRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityParticipantRepository participantRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatService chatService;

    @Transactional
    public ActivityInvitation createInvitation(Long activityId, Long senderId, Long receiverId) {
        if (Objects.equals(senderId, receiverId)) {
            throw new IllegalArgumentException("You cannot invite yourself.");
        }

        Activity activity = activityRepository.findById(activityId).orElse(null);
        if (activity == null) {
            throw new IllegalArgumentException("Activity does not exist.");
        }

        if (!Objects.equals(activity.getCreatorId(), senderId)) {
            throw new IllegalArgumentException("Only the activity creator can send invitations.");
        }

        User receiver = userRepository.findById(receiverId).orElse(null);
        if (receiver == null) {
            throw new IllegalArgumentException("Invitee does not exist.");
        }

        boolean areFriends =
                friendshipRepository.findByUserIdAndFriendId(senderId, receiverId) != null &&
                friendshipRepository.findByUserIdAndFriendId(receiverId, senderId) != null;
        if (!areFriends) {
            throw new IllegalArgumentException("You can only invite existing friends.");
        }

        if (participantRepository.findByActivityIdAndUserId(activityId, receiverId).isPresent()) {
            throw new IllegalArgumentException("This friend is already a participant.");
        }

        Optional<ActivityInvitation> existing = invitationRepository.findByActivityIdAndReceiverIdAndStatus(
                activityId, receiverId, "pending"
        );
        if (existing.isPresent()) {
            return existing.get();
        }

        ActivityInvitation invitation = new ActivityInvitation();
        invitation.setActivityId(activityId);
        invitation.setSenderId(senderId);
        invitation.setReceiverId(receiverId);
        invitation.setStatus("pending");
        ActivityInvitation saved = invitationRepository.save(invitation);
        chatService.sendMessage(
                senderId,
                receiverId,
                "[Activity Invitation] " + activity.getTitle() + " - open Applications to accept or decline."
        );
        return saved;
    }

    public Map<String, Object> getMyInvitations(Long currentUserId) {
        Map<String, Object> data = new HashMap<>();
        data.put("incoming", enrich(invitationRepository.findByReceiverIdOrderByCreateTimeDesc(currentUserId)));
        data.put("outgoing", enrich(invitationRepository.findBySenderIdOrderByCreateTimeDesc(currentUserId)));
        return data;
    }

    public List<ActivityInvitation> getPendingIncomingInvitations(Long currentUserId) {
        return invitationRepository.findByReceiverIdOrderByCreateTimeDesc(currentUserId).stream()
                .filter(invitation -> "pending".equalsIgnoreCase(invitation.getStatus()))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getInvitationDetail(Long invitationId, Long currentUserId) {
        ActivityInvitation invitation = invitationRepository.findById(invitationId).orElse(null);
        if (invitation == null) {
            return null;
        }

        if (!Objects.equals(invitation.getSenderId(), currentUserId) && !Objects.equals(invitation.getReceiverId(), currentUserId)) {
            return null;
        }

        return toPayload(invitation);
    }

    @Transactional
    public boolean handleInvitation(Long invitationId, boolean accept, Long currentUserId) {
        ActivityInvitation invitation = invitationRepository.findById(invitationId).orElse(null);
        if (invitation == null || !Objects.equals(invitation.getReceiverId(), currentUserId)) {
            return false;
        }

        if (!"pending".equalsIgnoreCase(invitation.getStatus())) {
            return false;
        }

        Activity activity = activityRepository.findById(invitation.getActivityId()).orElse(null);
        if (activity == null) {
            invitation.setStatus("cancelled");
            invitation.setHandleTime(LocalDateTime.now());
            invitationRepository.save(invitation);
            return false;
        }

        if (accept) {
            List<ActivityParticipant> currentParticipants = participantRepository.findByActivityId(activity.getId());
            if (activity.getMaxParticipants() != null && currentParticipants.size() >= activity.getMaxParticipants()) {
                invitation.setStatus("expired");
                invitation.setHandleTime(LocalDateTime.now());
                invitationRepository.save(invitation);
                return false;
            }

            if (participantRepository.findByActivityIdAndUserId(activity.getId(), currentUserId).isEmpty()) {
                ActivityParticipant participant = new ActivityParticipant();
                participant.setActivityId(activity.getId());
                participant.setUserId(currentUserId);
                participantRepository.save(participant);
            }

            activity.setStatus(1);
            activityRepository.save(activity);
        }

        invitation.setStatus(accept ? "accepted" : "declined");
        invitation.setHandleTime(LocalDateTime.now());
        invitationRepository.save(invitation);
        return true;
    }

    private List<Map<String, Object>> enrich(List<ActivityInvitation> invitations) {
        return invitations.stream()
                .map(this::toPayload)
                .collect(Collectors.toList());
    }

    private Map<String, Object> toPayload(ActivityInvitation invitation) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("invitation", invitation);
        payload.put("activity", activityRepository.findById(invitation.getActivityId()).orElse(null));
        payload.put("sender", userRepository.findById(invitation.getSenderId()).orElse(null));
        payload.put("receiver", userRepository.findById(invitation.getReceiverId()).orElse(null));
        return payload;
    }
}
