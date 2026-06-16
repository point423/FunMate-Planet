package com.zjgsu.pjt.backend.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class UploadService {

    @Value("${upload.dir:uploads/}")
    private String uploadDir;

    @Value("${upload.url:}")
    private String uploadUrl;

    public String uploadImage(MultipartFile file, HttpServletRequest request) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String newFilename = UUID.randomUUID() + ext;

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path targetLocation = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return resolvePublicBaseUrl(request) + newFilename;
    }

    private String resolvePublicBaseUrl(HttpServletRequest request) {
        String configuredBaseUrl = normalizeBaseUrl(uploadUrl);
        if (configuredBaseUrl != null) {
            return configuredBaseUrl;
        }

        if (request == null) {
            return "/static/";
        }

        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();

        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(host);
        if (shouldAppendPort(scheme, port)) {
            baseUrl.append(":").append(port);
        }
        baseUrl.append("/static/");
        return baseUrl.toString();
    }

    private String normalizeBaseUrl(String rawBaseUrl) {
        if (rawBaseUrl == null) {
            return null;
        }

        String trimmed = rawBaseUrl.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed.endsWith("/") ? trimmed : trimmed + "/";
    }

    private boolean shouldAppendPort(String scheme, int port) {
        return port > 0
                && !(port == 80 && "http".equalsIgnoreCase(scheme))
                && !(port == 443 && "https".equalsIgnoreCase(scheme));
    }
}
