package com.zjgsu.pjt.backend.entity;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Setter;
import lombok.AccessLevel;
import java.time.LocalDateTime;
import java.util.Map;

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
    
    @Setter(AccessLevel.NONE)
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

    @JsonSetter("avatar")
    public void setAvatar(Object avatar) {
        if (avatar instanceof String) {
            this.avatar = (String) avatar;
        } else if (avatar instanceof Map) {
            Object url = ((Map<?, ?>) avatar).get("url");
            this.avatar = (url != null) ? url.toString() : null;
        }
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
