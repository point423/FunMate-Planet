package com.zjgsu.pjt.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjgsu.pjt.backend.entity.ActivityInvitation;
import com.zjgsu.pjt.backend.service.ActivityInvitationService;
import com.zjgsu.pjt.backend.util.JwtUtil;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ActivityInvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityInvitationService activityInvitationService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private final String DUMMY_TOKEN = "Bearer dummy-token";

    @Test
    @DisplayName("获取我的活动邀请")
    void getInvitations_Success() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("incoming", List.of(Map.of("id", 1)));
        payload.put("outgoing", List.of());

        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(activityInvitationService.getMyInvitations(1L)).thenReturn(payload);

        mockMvc.perform(get("/api/activity-invitations")
                        .header("Authorization", DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.incoming[0].id").value(1));
    }

    @Test
    @DisplayName("处理活动邀请")
    void handleInvitation_Success() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(2L);
        when(activityInvitationService.handleInvitation(99L, true, 2L)).thenReturn(true);

        mockMvc.perform(post("/api/activity-invitations/99/handle")
                        .header("Authorization", DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("accept", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("创建活动邀请")
    void createInvitation_Success() throws Exception {
        ActivityInvitation invitation = new ActivityInvitation();
        invitation.setId(1L);
        invitation.setActivityId(10L);
        invitation.setSenderId(1L);
        invitation.setReceiverId(2L);

        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(activityInvitationService.createInvitation(10L, 1L, 2L)).thenReturn(invitation);

        mockMvc.perform(post("/api/activity-invitations")
                        .header("Authorization", DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("activityId", 10, "receiverId", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.activityId").value(10));
    }
}
