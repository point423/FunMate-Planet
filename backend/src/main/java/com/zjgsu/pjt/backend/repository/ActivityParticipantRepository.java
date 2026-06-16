package com.zjgsu.pjt.backend.repository;

import com.zjgsu.pjt.backend.entity.ActivityParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, Long> {
    List<ActivityParticipant> findByActivityId(Long activityId);
    List<ActivityParticipant> findByUserId(Long userId);
    Optional<ActivityParticipant> findByActivityIdAndUserId(Long activityId, Long userId);
}
