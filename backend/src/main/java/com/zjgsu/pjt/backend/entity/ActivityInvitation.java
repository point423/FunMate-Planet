package com.zjgsu.pjt.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_invitation")
@Data
public class ActivityInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(nullable = false)
    private String status = "pending";

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "handle_time")
    private LocalDateTime handleTime;
}
