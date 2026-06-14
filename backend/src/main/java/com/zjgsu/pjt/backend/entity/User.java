package com.zjgsu.pjt.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "user")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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
    private Integer gender; 
    private String tags;
    private String bio;

    private Double longitude;
    private Double latitude;

    @Column(name = "average_score")
    private Double averageScore = 0.0; // Positive feedback rate: 3-star reviews / total reviews * 100

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    @JsonSetter("avatar")
    public void setAvatarFromObject(Object avatarObj) {
        if (avatarObj instanceof String) {
            this.avatar = (String) avatarObj;
        } else if (avatarObj instanceof Map) {
            Object url = ((Map<?, ?>) avatarObj).get("url");
            this.avatar = (url != null) ? url.toString() : null;
        }
    }
}
