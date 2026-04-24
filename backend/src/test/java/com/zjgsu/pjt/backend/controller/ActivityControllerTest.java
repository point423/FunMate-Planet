package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.service.ActivityService;
import com.zjgsu.pjt.backend.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityService activityService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private final String DUMMY_TOKEN = "Bearer dummy-token";

    @Test
    @DisplayName("测试创建活动接口")
    void createActivity_Success() throws Exception {
        Activity activity = new Activity();
        activity.setTitle("测试活动");

        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(activityService.createActivity(any())).thenReturn(activity);

        mockMvc.perform(post("/api/activities")
                        .header("Authorization", DUMMY_TOKEN) // ✅ 必须携带 Token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activity)))
                .andExpect(status().isOk()) // ✅ 后端返回 200 (Result 封装)
                .andExpect(jsonPath("$.code").value(0)); // ✅ 作业要求 code 0
    }

    @Test
    @DisplayName("测试获取活动列表接口")
    void listActivities_Success() throws Exception {
        // 白名单中没有 /api/activities，需要携带 Token
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);

        mockMvc.perform(get("/api/activities")
                        .header("Authorization", DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("测试删除活动接口")
    void deleteActivity_Success() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(activityService.deleteActivity(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/activities/1")
                        .header("Authorization", DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("success"));
    }
}
