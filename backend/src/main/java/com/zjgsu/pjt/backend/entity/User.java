package com.zjgsu.pjt.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;
    private String nickname;
    private String avatar;
    private Integer age;
    private Integer gender; // 0:保密, 1:男, 2:女
    private String tags;
    private String bio;

    private Double longitude;
    private Double latitude;

    @Column(name = "average_score")
    private Double averageScore = 0.0;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();
}
