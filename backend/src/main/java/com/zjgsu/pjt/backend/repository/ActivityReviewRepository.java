package com.zjgsu.pjt.backend.repository;

import com.zjgsu.pjt.backend.entity.ActivityReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActivityReviewRepository extends JpaRepository<ActivityReview, Long> {
    List<ActivityReview> findByActivityId(Long activityId);
    List<ActivityReview> findByRevieweeId(Long revieweeId);
}
