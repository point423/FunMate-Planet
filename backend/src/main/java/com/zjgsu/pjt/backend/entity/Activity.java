package com.zjgsu.pjt.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity")
@Data
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "activity_time")
    private LocalDateTime activityTime;

    private String location;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    private Integer status = 0; // 0:招募中, 1:进行中, 2:已结束

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();
}
