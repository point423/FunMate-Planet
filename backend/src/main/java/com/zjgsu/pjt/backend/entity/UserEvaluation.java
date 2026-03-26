package com.zjgsu.pjt.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_evaluation")
@Data
public class UserEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evaluator_id")
    private Long evaluatorId;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "score_level")
    private Integer scoreLevel; // 1:低, 2:中, 3:高

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();
}
