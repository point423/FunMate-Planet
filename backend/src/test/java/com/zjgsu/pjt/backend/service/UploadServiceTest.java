package com.zjgsu.pjt.backend.service;


import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.junit.jupiter.api.io.TempDir;

import org.mockito.InjectMocks;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

import java.io.InputStream;

import java.nio.file.Files;

import java.nio.file.Path;


import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)

public class UploadServiceTest {


    @Mock

    private MultipartFile file;


    @InjectMocks

    private UploadService uploadService;


    @TempDir

    Path tempDir;


    @BeforeEach

    void setUp() {

        ReflectionTestUtils.setField(uploadService, "uploadDir", tempDir.toString() + "/");

        ReflectionTestUtils.setField(uploadService, "uploadUrl", "http://localhost:8080/static/");

    }


    @Test

    @DisplayName("上传图片-成功")

    void uploadImage_Success() throws IOException {

        // Given

        when(file.isEmpty()).thenReturn(false);

        when(file.getOriginalFilename()).thenReturn("test.png");

        when(file.getInputStream()).thenReturn(InputStream.nullInputStream());


        // When

        String url = uploadService.uploadImage(file);


        // Then

        assertNotNull(url);

        assertTrue(url.contains("test.png") || url.contains(".png"));

        assertTrue(url.startsWith("http://localhost:8080/static/"));

    }


    @Test

    @DisplayName("上传图片-空文件异常")

    void uploadImage_EmptyFile_Throws() {

        // Given

        when(file.isEmpty()).thenReturn(true);


        // When & Then

        assertThrows(IllegalArgumentException.class, () -> uploadService.uploadImage(file));

    }


    @Test

    @DisplayName("上传图片-无扩展名默认.jpg")

    void uploadImage_NoExtension_DefaultsToJpg() throws IOException {

        // Given

        when(file.isEmpty()).thenReturn(false);

        when(file.getOriginalFilename()).thenReturn("photo");

        when(file.getInputStream()).thenReturn(InputStream.nullInputStream());


        // When

        String url = uploadService.uploadImage(file);


        // Then

        assertNotNull(url);

        assertTrue(url.endsWith(".jpg"));

    }


    @Test

    @DisplayName("上传图片-空文件名使用UUID")

    void uploadImage_NullFilename_UsesUUID() throws IOException {

        // Given

        when(file.isEmpty()).thenReturn(false);

        when(file.getOriginalFilename()).thenReturn(null);

        when(file.getInputStream()).thenReturn(InputStream.nullInputStream());


        // When

        String url = uploadService.uploadImage(file);


        // Then

        assertNotNull(url);

        // UUID 格式的 .jpg 文件名

        assertTrue(url.matches(".*[a-f0-9-]{36}\\.jpg"));

    }

}

