package com.zjgsu.pjt.backend.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileStorageUtilTest {

    private static final String BASE_URL = "http://localhost:8080/static/";

    @Test
    void saveFile_ReturnsNullForNullOrEmptyFile() {
        assertThat(FileStorageUtil.saveFile(null)).isNull();
        assertThat(FileStorageUtil.saveFile(new MockMultipartFile("file", new byte[0]))).isNull();
    }

    @Test
    void saveFile_WritesFileAndKeepsOriginalExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "abc".getBytes());

        String url = FileStorageUtil.saveFile(file);

        assertThat(url).startsWith(BASE_URL).endsWith(".png");
        Path savedPath = savedPathFromUrl(url);
        assertThat(Files.exists(savedPath)).isTrue();
        Files.deleteIfExists(savedPath);
    }

    @Test
    void saveFile_HandlesOriginalNameWithoutExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar", "image/jpeg", "abc".getBytes());

        String url = FileStorageUtil.saveFile(file);

        assertThat(url).startsWith(BASE_URL);
        String filename = url.substring(BASE_URL.length());
        assertThat(filename).doesNotContain(".");
        Files.deleteIfExists(savedPathFromUrl(url));
    }

    @Test
    void saveFile_ReturnsNullWhenTransferFails() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("photo.png");
        doThrow(new IOException("disk full")).when(file).transferTo(any(File.class));

        String url = withMutedErr(() -> FileStorageUtil.saveFile(file));

        assertThat(url).isNull();
        verify(file).transferTo(any(File.class));
    }

    @Test
    void constants_HaveExpectedPublicStorageShape() throws Exception {
        java.lang.reflect.Field baseUrlField = FileStorageUtil.class.getDeclaredField("BASE_URL");
        baseUrlField.setAccessible(true);
        String baseUrl = (String) baseUrlField.get(null);

        java.lang.reflect.Field uploadDirField = FileStorageUtil.class.getDeclaredField("UPLOAD_DIR");
        uploadDirField.setAccessible(true);
        String uploadDir = (String) uploadDirField.get(null);

        assertThat(baseUrl).startsWith("http").contains("static").endsWith("/");
        assertThat(uploadDir).contains("uploads").endsWith(File.separator);
    }

    private Path savedPathFromUrl(String url) {
        String filename = url.substring(BASE_URL.length());
        return Path.of(System.getProperty("user.dir"), "uploads", filename);
    }

    private String withMutedErr(java.util.function.Supplier<String> action) {
        PrintStream original = System.err;
        ByteArrayOutputStream ignored = new ByteArrayOutputStream();
        PrintStream muted = new PrintStream(ignored);
        try {
            System.setErr(muted);
            return action.get();
        } finally {
            System.setErr(original);
            muted.close();
        }
    }
}
