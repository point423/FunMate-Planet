package com.zjgsu.pjt.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_participant")
@Data
public class ActivityParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "join_time")
    private LocalDateTime joinTime = LocalDateTime.now();
    
    private Integer status = 1; // 1: 已加入, 2: 已退出
}
