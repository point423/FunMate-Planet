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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    @DisplayName("测试发布日记接口")
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
    @DisplayName("测试获取日记列表接口")
    void listDiaries_Success() throws Exception {
        // ✅ 核心修复：Mock Token 解析并添加 Header 避免 401 报错
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(diaryService.getDiaries(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/diaries")
                        .header("Authorization", DUMMY_TOKEN)) // ✅ 补全 Header
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
