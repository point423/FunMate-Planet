package com.zjgsu.pjt.backend.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class UserProfileResponse {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String bio;
    private List<String> tags = new ArrayList<>();
    private Double longitude;
    private Double latitude;
    private String createdAt;

    private Integer activities = 0;
    private Double score = 0.0;
    private Object ranking = "--";

    private Double distance = 0.0;
    private List<Map<String, Object>> publicJournals = new ArrayList<>();
    private List<Map<String, Object>> recentActivities = new ArrayList<>();
}
