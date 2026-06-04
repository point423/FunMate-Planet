package com.zjgsu.pjt.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_review")
@Data
public class ActivityReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "reviewee_id")
    private Long revieweeId;

    private Integer rating; // 1-5 星

    private String comment;

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();
}
