package com.zjgsu.pjt.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UploadServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    void uploadImage_WritesFileAndReturnsPublicUrl() throws Exception {
        UploadService uploadService = uploadService();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "image-content".getBytes()
        );

        String url = uploadService.uploadImage(file);

        assertThat(url).startsWith("https://cdn.example/static/");
        assertThat(url).endsWith(".png");
        String filename = url.substring("https://cdn.example/static/".length());
        assertThat(Files.exists(tempDir.resolve(filename))).isTrue();
    }

    @Test
    void uploadImage_UsesDefaultExtensionWhenOriginalNameHasNoExtension() throws Exception {
        UploadService uploadService = uploadService();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar",
                "image/jpeg",
                "image-content".getBytes()
        );

        String url = uploadService.uploadImage(file);

        assertThat(url).endsWith(".jpg");
    }

    @Test
    void uploadImage_UsesUuidAndDefaultExtensionWhenOriginalNameIsNull() throws Exception {
        UploadService uploadService = uploadService();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                null,
                "image/jpeg",
                "image-content".getBytes()
        );

        String url = uploadService.uploadImage(file);

        assertThat(url).matches("https://cdn\\.example/static/[a-f0-9-]{36}\\.jpg");
    }

    @Test
    void uploadImage_RejectsNullOrEmptyFile() {
        UploadService uploadService = uploadService();

        assertThatThrownBy(() -> uploadService.uploadImage(null))
                .isInstanceOf(IllegalArgumentException.class);

        MockMultipartFile empty = new MockMultipartFile("file", new byte[0]);
        assertThatThrownBy(() -> uploadService.uploadImage(empty))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private UploadService uploadService() {
        UploadService uploadService = new UploadService();
        ReflectionTestUtils.setField(uploadService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(uploadService, "uploadUrl", "https://cdn.example/static/");
        return uploadService;
    }
}
