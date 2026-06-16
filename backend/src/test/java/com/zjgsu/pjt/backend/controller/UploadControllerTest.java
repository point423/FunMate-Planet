package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.service.UploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadControllerTest {

    @Mock
    private UploadService uploadService;

    private UploadController controller;

    @BeforeEach
    void setUp() {
        controller = new UploadController();
        ReflectionTestUtils.setField(controller, "uploadService", uploadService);
    }

    @Test
    void uploadImage_ReturnsCreatedUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", "x".getBytes());
        when(uploadService.uploadImage(file)).thenReturn("https://cdn.example/a.png");

        Result<Map<String, String>> result = controller.uploadImage(file, 1L);

        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getMessage()).isEqualTo("created");
        assertThat(result.getData()).containsEntry("url", "https://cdn.example/a.png");
    }

    @Test
    void uploadImage_ReturnsBadRequestForInvalidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", new byte[0]);
        when(uploadService.uploadImage(file)).thenThrow(new IllegalArgumentException("empty"));

        Result<Map<String, String>> result = controller.uploadImage(file, 1L);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("empty");
    }

    @Test
    void uploadImage_ReturnsServerErrorForIoException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", "x".getBytes());
        when(uploadService.uploadImage(file)).thenThrow(new IOException("disk full"));

        Result<Map<String, String>> result = controller.uploadImage(file, 1L);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("disk full");
    }
}
