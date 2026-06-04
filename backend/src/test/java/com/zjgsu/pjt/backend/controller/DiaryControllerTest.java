package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.entity.ActivityDiary;
import com.zjgsu.pjt.backend.service.DiaryService;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DiaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiaryService diaryService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private final String DUMMY_TOKEN = "Bearer dummy-token";

    @Test
    @DisplayName("1. 发布日记-成功场景")
    void createDiary_Success() throws Exception {
        ActivityDiary diary = new ActivityDiary();
        diary.setContent("接口测试日记");

        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(diaryService.createDiary(any())).thenReturn(diary);

        mockMvc.perform(post("/api/diaries")
                        .header("Authorization", DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diary)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("2. 安全校验-修改他人日记应返回403")
    void updateDiary_Forbidden() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        // 模拟 Service 返回 null 代表越权校验失败
        when(diaryService.updateDiary(eq(1L), any(), eq(1L))).thenReturn(null);

        mockMvc.perform(put("/api/diaries/1")
                        .header("Authorization", DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hacked\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("3. 安全校验-删除他人日记应返回403")
    void deleteDiary_Forbidden() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(diaryService.deleteDiary(eq(1L), eq(1L))).thenReturn(false);

        mockMvc.perform(delete("/api/diaries/1")
                        .header("Authorization", DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }
}
