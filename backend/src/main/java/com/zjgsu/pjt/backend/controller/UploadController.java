package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class UploadController {

    @Value("${upload.dir:uploads/}")
    private String uploadDir;

    @Value("${upload.url:http://localhost:8080/static/}")
    private String uploadUrl;

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file,
                                                   HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        if (currentUserId == null) {
            return Result.error(401, "未授权");
        }

        try {
            if (file == null || file.isEmpty()) {
                return Result.error(400, "文件为空");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank() || !originalFilename.contains(".")) {
                return Result.error(400, "文件名无效");
            }

            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID() + ext;

            Path dirPath = Paths.get(uploadDir);
            Files.createDirectories(dirPath);

            Path targetPath = dirPath.resolve(newFilename);
            file.transferTo(new File(targetPath.toString()));

            Map<String, String> data = new HashMap<>();
            data.put("url", uploadUrl + newFilename);
            return Result.created(data);
        } catch (Exception e) {
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }
}