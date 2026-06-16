package com.zjgsu.pjt.backend.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * FileStorageUtil 测试
 *
 * 注意:由于 UPLOAD_DIR 是 static final 常量,Java 17+ 反射修改会失败,
 * 所以本测试只覆盖不依赖文件系统的纯逻辑分支:
 *   - 空文件
 *   - null 文件
 *   - IO 异常
 *   - 常量值合理性
 *
 * 真正落盘的逻辑建议重构为可注入后再测。
 */
@ExtendWith(MockitoExtension.class)
public class FileStorageUtilTest {

    @Mock
    private MultipartFile file;

    @Test
    @DisplayName("saveFile-null 文件返回 null")
    void saveFile_Null_ReturnsNull() {
        String result = FileStorageUtil.saveFile(null);
        assertNull(result);
    }

    @Test
    @DisplayName("saveFile-空文件返回 null")
    void saveFile_Empty_ReturnsNull() {
        when(file.isEmpty()).thenReturn(true);

        String result = FileStorageUtil.saveFile(file);
        assertNull(result);
        verify(file).isEmpty();
    }

    @Test
    @DisplayName("saveFile-IO 异常被捕获,返回 null(不向上抛)")
    void saveFile_TransferError_ReturnsNull() throws IOException {
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("photo.png");
        // 模拟 transferTo 抛异常(磁盘满 / 权限不足等)
        doThrow(new IOException("磁盘空间不足")).when(file).transferTo(any(File.class));

        String result = FileStorageUtil.saveFile(file);
        assertNull(result);
    }

    @Test
    @DisplayName("常量 BASE_URL-以 http:// 开头并以 /static/ 结尾")
    void baseUrl_HasCorrectFormat() throws Exception {
        java.lang.reflect.Field field = FileStorageUtil.class.getDeclaredField("BASE_URL");
        field.setAccessible(true);
        String baseUrl = (String) field.get(null);

        assertNotNull(baseUrl);
        assertTrue(baseUrl.startsWith("http://") || baseUrl.startsWith("https://"),
                "BASE_URL 应该是 HTTP/HTTPS 协议");
        assertTrue(baseUrl.endsWith("/"), "BASE_URL 应该以 / 结尾");
        assertTrue(baseUrl.contains("static"), "BASE_URL 应该包含 static 路径");
    }

    @Test
    @DisplayName("常量 UPLOAD_DIR-包含 uploads 目录名")
    void uploadDir_ContainsUploadsName() throws Exception {
        java.lang.reflect.Field field = FileStorageUtil.class.getDeclaredField("UPLOAD_DIR");
        field.setAccessible(true);
        String uploadDir = (String) field.get(null);

        assertNotNull(uploadDir);
        assertTrue(uploadDir.contains("uploads"),
                "UPLOAD_DIR 应该包含 'uploads' 目录名,实际值: " + uploadDir);
        assertTrue(uploadDir.endsWith(File.separator) || uploadDir.endsWith("/") || uploadDir.endsWith("\\"),
                "UPLOAD_DIR 应该以路径分隔符结尾,实际值: " + uploadDir);
    }
}