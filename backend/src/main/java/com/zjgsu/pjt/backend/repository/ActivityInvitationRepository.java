package com.zjgsu.pjt.backend.repository;

import com.zjgsu.pjt.backend.entity.ActivityInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityInvitationRepository extends JpaRepository<ActivityInvitation, Long> {
    Optional<ActivityInvitation> findByActivityIdAndReceiverIdAndStatus(Long activityId, Long receiverId, String status);
    List<ActivityInvitation> findByReceiverIdOrderByCreateTimeDesc(Long receiverId);
    List<ActivityInvitation> findBySenderIdOrderByCreateTimeDesc(Long senderId);
    List<ActivityInvitation> findByActivityId(Long activityId);
}
