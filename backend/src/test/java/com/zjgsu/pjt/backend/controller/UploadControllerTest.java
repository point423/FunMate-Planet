package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.service.UploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UploadController 测试
 *
 * 注意:
 * 1. standaloneSetup 不走 AuthInterceptor,所以 currentUserId 不会自动注入
 * 2. 用 .requestAttr("currentUserId", 1L) 手动注入
 * 3. Result.success() 返回 code=0(不是 200),HTTP 状态固定 200
 */
@ExtendWith(MockitoExtension.class)
public class UploadControllerTest {

    @Mock
    private UploadService uploadService;

    @InjectMocks
    private UploadController uploadController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(uploadController).build();
    }

    @Test
    @DisplayName("POST /api/upload/image-成功返回 URL")
    void uploadImage_Success() throws Exception {
        when(uploadService.uploadImage(any()))
                .thenReturn("http://localhost:8080/static/abc.png");

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "test content".getBytes());

        mockMvc.perform(multipart("/api/upload/image")
                        .file(file)
                        .requestAttr("currentUserId", 1L))   // 手动注入拦截器才会注入的属性
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("created"))
                .andExpect(jsonPath("$.data.url").value("http://localhost:8080/static/abc.png"));
    }

    @Test
    @DisplayName("POST /api/upload/image-空文件,业务码 400")
    void uploadImage_EmptyFile() throws Exception {
        when(uploadService.uploadImage(any()))
                .thenThrow(new IllegalArgumentException("文件为空"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "test content".getBytes());

        mockMvc.perform(multipart("/api/upload/image")
                        .file(file)
                        .requestAttr("currentUserId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("文件为空"));
    }

    @Test
    @DisplayName("POST /api/upload/image-IO 异常,业务码 500")
    void uploadImage_IOError() throws Exception {
        when(uploadService.uploadImage(any()))
                .thenThrow(new IOException("磁盘满"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "test content".getBytes());

        mockMvc.perform(multipart("/api/upload/image")
                        .file(file)
                        .requestAttr("currentUserId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("磁盘满")));
    }
}