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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
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
    @DisplayName("1. 创建活动-成功场景")
    void createActivity_Success() throws Exception {
        Activity activity = new Activity();
        activity.setTitle("测试活动");
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(activityService.createActivity(any())).thenReturn(activity);

        mockMvc.perform(post("/api/activities")
                        .header("Authorization", DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("2. 安全校验-非创建者修改活动应返回403")
    void updateActivity_Forbidden() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        // 模拟 Service 返回 null 代表权限校验不通过
        when(activityService.updateActivity(eq(1L), any(), eq(1L))).thenReturn(null);

        mockMvc.perform(put("/api/activities/1")
                        .header("Authorization", DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"hacked\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("3. 安全校验-非创建者删除活动应返回403")
    void deleteActivity_Forbidden() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(activityService.deleteActivity(eq(1L), eq(1L))).thenReturn(false);

        mockMvc.perform(delete("/api/activities/1")
                        .header("Authorization", DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("4. 鎴戠殑娲诲姩-鎸夌姸鎬佸垎缁勮繑鍥�")
    void myActivities_ReturnsGroupedActivities() throws Exception {
        Activity pending = new Activity();
        pending.setId(10L);
        pending.setTitle("Pending activity");

        Map<String, List<Activity>> grouped = new LinkedHashMap<>();
        grouped.put("pending", List.of(pending));
        grouped.put("active", List.of());
        grouped.put("completed", List.of());
        grouped.put("cancelled", List.of());

        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(activityService.getMyActivities(1L)).thenReturn(grouped);

        mockMvc.perform(get("/api/activities/my")
                        .header("Authorization", DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.pending[0].title").value("Pending activity"))
                .andExpect(jsonPath("$.data.active").isArray())
                .andExpect(jsonPath("$.data.completed").isArray())
                .andExpect(jsonPath("$.data.cancelled").isArray());
    }

    @Test
    @DisplayName("5. 娲诲姩璇︽儏-杩斿洖hasJournal瀛楁")
    void detail_ReturnsHasJournal() throws Exception {
        Activity activity = new Activity();
        activity.setId(10L);
        activity.setTitle("Activity with diary");

        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(activityService.findById(10L)).thenReturn(activity);
        when(activityService.getParticipants(10L)).thenReturn(List.of());
        when(activityService.hasJournal(10L)).thenReturn(true);

        mockMvc.perform(get("/api/activities/10")
                        .header("Authorization", DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activity.id").value(10))
                .andExpect(jsonPath("$.data.participantCount").value(0))
                .andExpect(jsonPath("$.data.hasJournal").value(true));
    }
}
