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

    @PostMapping("/suggest")
    public Result getSuggestion(@RequestBody Map<String, String> request) {
        String userTags = request.getOrDefault("tags", "");
        String location = request.getOrDefault("location", "");
        String query = request.getOrDefault("query", "");

        String suggestion = aiService.generateActivitySuggestion(userTags, location, query);

        return Result.success(Map.of("suggestion", suggestion));
    }
}
