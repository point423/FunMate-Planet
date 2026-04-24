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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    @DisplayName("测试评价伙伴接口")
    void evaluate_Success() throws Exception {
        UserEvaluation eval = new UserEvaluation();
        eval.setTargetId(2L);
        eval.setScoreLevel(3);

        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);

        mockMvc.perform(post("/api/evaluations")
                        .header("Authorization", "Bearer dummy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eval)))
                .andExpect(status().isOk()) // ✅ 修正：由 isCreated 改为 isOk
                .andExpect(jsonPath("$.code").value(0));
    }
}
