package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    /**
     * AI 连通性测试接口
     */
    @GetMapping("/test")
    public Result<String> testAi() {
        String suggestion = aiService.generateActivitySuggestion("测试标签", "测试位置", "你好，请说一句话证明你在线。");
        return Result.success(suggestion);
    }

    @PostMapping("/suggest")
    public Result getSuggestion(@RequestBody Map<String, String> request) {
        String userTags = request.getOrDefault("tags", "");
        String location = request.getOrDefault("location", "");
        String query = request.getOrDefault("query", "");
        String suggestion = aiService.generateActivitySuggestion(userTags, location, query);
        return Result.success(Map.of("suggestion", suggestion));
    }

    @PostMapping("/activity-summary")
    public Result getSummary(@RequestBody Map<String, String> request) {
        String title = request.getOrDefault("title", "未命名活动");
        String participants = request.getOrDefault("participants", "多人");
        String reviews = request.getOrDefault("reviews", "暂无详细评价");
        
        String summary = aiService.generateActivitySummary(title, participants, reviews);
        return Result.success(Map.of("summary", summary));
    }
}
