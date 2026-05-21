package com.zjgsu.pjt.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

@Service
public class AiService {

    // 将默认值改为 localhost 以适配您的测试环境
    @Value("${ai.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${ai.model:qwen2.5:1.5b}")
    private String model;

    /**
     * 生成活动建议
     */
    public String generateActivitySuggestion(String userTags, String userLocation, String userQuery) {
        String prompt = String.format("你是一个社交星球助手。用户标签[%s]，位置[%s]。请求：%s。请给出100字内的幽默风趣建议。",
                userTags, userLocation, userQuery);
        return callAi(prompt);
    }

    /**
     * 生成活动总结 (修复 AiController 编译错误)
     * 接收三个 String 参数：title, participants, reviews
     */
    public String generateActivitySummary(String title, String participants, String reviews) {
        String prompt = String.format("请为活动'%s'写一段简短的总结。参与者：%s。评价回顾：%s要求：字数50字左右，风格积极向上，带一点社交感。",
                title, participants, reviews);
        return callAi(prompt);
    }

    /**
     * 统一的 AI 调用逻辑
     */
    private String callAi(String prompt) {
        // 处理 URL 拼接
        String url = baseUrl.trim();
        if (url.endsWith("/v1")) url = url.substring(0, url.length() - 3);
        if (!url.endsWith("/")) url += "/";
        url += "api/generate";

        // 处理 JSON 转义，防止特殊字符导致 Payload 损坏
        String escapedPrompt = prompt.replace("\\", "\\\\")
                                     .replace("\"", "\\\"")
                                     .replace("\n", "\\n")
                                     .replace("\r", "\\r");

        String jsonPayload = String.format("""
            {
                "model": "%s",
                "prompt": "%s",
                "stream": false
            }
            """, model, escapedPrompt);

        System.out.println(">>> [AI-Direct] 发起请求 URL: " + url);

        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20)) // 增加建立连接的超时
                .build()) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(600)) // 将总超时正式改为 600 秒
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String body = response.body();
                // 健壮的 JSON 响应提取逻辑
                try {
                    int start = body.indexOf("\"response\":\"") + 12;
                    int end = body.indexOf("\"", start);
                    // 循环寻找未转义的引号作为结束
                    while (end > 0 && body.charAt(end - 1) == '\\') {
                        end = body.indexOf("\"", end + 1);
                    }
                    if (start > 11 && end > start) {
                        return body.substring(start, end)
                                .replace("\\n", "\n")
                                .replace("\\\"", "\"");
                    }
                } catch (Exception e) {
                    System.err.println(">>> [AI-Direct] 解析响应失败，全文: " + body);
                }
                return body;
            } else {
                return "AI 助手繁忙 (HTTP " + response.statusCode() + ")";
            }
        } catch (HttpTimeoutException te) {
            System.err.println(">>> [AI-Direct] 调用超时 (600s): " + te.getMessage());
            return "AI 思考时间过长，请确保模型已下载且硬件资源充足。";
        } catch (Exception e) {
            System.err.println(">>> [AI-Direct] 调用异常: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return "AI 助手连接失败 (" + e.getMessage() + ")";
        }
    }
}
