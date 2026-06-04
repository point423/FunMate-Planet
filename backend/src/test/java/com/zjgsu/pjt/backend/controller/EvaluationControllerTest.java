package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.entity.UserEvaluation;
import com.zjgsu.pjt.backend.service.EvaluationService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EvaluationService evaluationService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private final String DUMMY_TOKEN = "Bearer dummy";

    @Test
    @DisplayName("1. 评价伙伴-成功场景")
    void evaluate_Success() throws Exception {
        UserEvaluation eval = new UserEvaluation();
        eval.setTargetId(2L);
        eval.setScoreLevel(3);

        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);

        mockMvc.perform(post("/api/evaluations")
                        .header("Authorization", DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eval)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("2. 安全加固校验-修改他人评价应返回403")
    void updateEvaluation_Forbidden() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        // 模拟 Service 返回 null 代表越权
        when(evaluationService.updateEvaluation(eq(1L), any(), eq(1L))).thenReturn(null);

        mockMvc.perform(put("/api/evaluations/1")
                        .header("Authorization", DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scoreLevel\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("3. 安全加固校验-删除他人评价应返回403")
    void deleteEvaluation_Forbidden() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(evaluationService.deleteEvaluation(eq(99L), eq(1L))).thenReturn(false);

        mockMvc.perform(delete("/api/evaluations/99")
                        .header("Authorization", DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }
}
