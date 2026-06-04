package com.zjgsu.pjt.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Setter;
import lombok.AccessLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "activity_diary")
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略前端多传的未知字段
public class ActivityDiary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "activity_id")
    private Long activityId;

    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(columnDefinition = "TEXT")
    @Setter(AccessLevel.NONE) // 禁用 Lombok 生成的 Setter，统一走下面自定义的
    private String images; 

    @Column(columnDefinition = "TEXT")
    @Setter(AccessLevel.NONE) // 禁用 Lombok 生成的 Setter
    private String tags;

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 终极容错：处理 images (支持数组、对象、字符串)
     */
    @JsonSetter("images")
    public void setImages(Object images) {
        this.images = convertObjectToString(images);
    }

    /**
     * 终极容错：处理 tags (支持数组、对象、字符串)
     * 解决 TagSelector 传来数组导致的 500 错误
     */
    @JsonSetter("tags")
    public void setTags(Object tags) {
        this.tags = convertObjectToString(tags);
    }

    private String convertObjectToString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) return (String) obj;
        try {
            // 将 List 或 Map 序列化为字符串存储
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    // 供业务代码调用的普通 setter
    public void setImages(String images) { this.images = images; }
    public void setTags(String tags) { this.tags = tags; }
}
