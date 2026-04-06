package com.zjgsu.pjt.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "friend_request")
@Data
public class FriendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "receiver_id")
    private Long receiverId;

    private String status = "pending"; // pending, accepted, declined

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();
}
