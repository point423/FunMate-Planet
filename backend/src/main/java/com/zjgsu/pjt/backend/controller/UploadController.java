package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.service.UploadService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    /**
     * 图片上传接口
     * 逻辑：已迁移至 UploadService，Controller 保持纯净
     */
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file,
                                                   @RequestAttribute("currentUserId") Long currentUserId) {
        try {
            String url = uploadService.uploadImage(file);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.created(data);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (IOException e) {
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }
}
