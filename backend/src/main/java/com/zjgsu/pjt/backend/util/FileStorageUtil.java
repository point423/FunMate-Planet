package com.zjgsu.pjt.backend.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileStorageUtil {

    // 使用项目运行目录下的 uploads 文件夹的绝对路径
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
    private static final String BASE_URL = "http://localhost:8080/static/";

    public static String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        File dir = new File(UPLOAD_DIR);
        // 确保目录存在，如果不存在则创建
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String newFilename = UUID.randomUUID().toString() + extension;
        File dest = new File(UPLOAD_DIR + newFilename);

        try {
            file.transferTo(dest.getAbsoluteFile()); // 使用绝对路径保存
            return BASE_URL + newFilename;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
