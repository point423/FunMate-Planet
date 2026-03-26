package com.zjgsu.pjt.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_diary")
@Data
public class ActivityDiary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "activity_id")
    private Long activityId;

    private String content;
    
    @Column(columnDefinition = "TEXT")
    private String images; // JSON 数组格式存储图片链接

    private String tags;

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();
}
