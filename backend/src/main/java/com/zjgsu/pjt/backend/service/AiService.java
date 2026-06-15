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

    /**
     * 兼容 OpenAI Chat Completions 协议的 base URL。
     * 例如:
     *   - https://api.deepseek.com/v1
     *   - https://api.openai.com/v1
     *   - http://ollama:11434/v1            (本地 Ollama,需 0.1.14+)
     *
     * 重要:Spring 内部将环境变量 AI_BASE_URL / AI_API_KEY 解析为
     *   ai.base.url / ai.api.key (下划线转点),@Value 严格匹配,
     *   所以这里要用点分形式才能正确读取。
     */
    @Value("${ai.base.url:https://api.deepseek.com/v1}")
    private String baseUrl;

    @Value("${ai.model:deepseek-chat}")
    private String model;

    @Value("${ai.api.key:}")
    private String apiKey;

    /**
     * 生成活动建议
     */
    public String generateActivitySuggestion(String userTags, String userLocation, String userQuery) {
        String userMessage = String.format(
                "用户标签[%s],位置[%s]。请求:%s。请用 100 字以内、幽默风趣的口吻给出活动建议。",
                userTags, userLocation, userQuery);

        return chatCompletion(
                "你是一个叫「趣搭星球小助手」的搭子匹配顾问,熟悉中国年轻人的线下兴趣社交场景。",
                userMessage);
    }

    /**
     * 统一的 OpenAI 兼容 Chat Completions 调用。
     * 适配:
     *   - DeepSeek
     *   - OpenAI
     *   - Ollama (0.1.14+ 暴露 /v1/chat/completions)
     *   - 其他所有 OpenAI 兼容服务
     */
    private String chatCompletion(String systemPrompt, String userMessage) {
        // 1. 拼接 URL: 去掉尾部斜杠,补 /chat/completions
        String url = baseUrl.trim();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        url += "/chat/completions";

        // 2. 构造 JSON 请求体 (OpenAI messages 格式)
        String jsonPayload = String.format("""
            {
                "model": "%s",
                "messages": [
                    {"role": "system", "content": "%s"},
                    {"role": "user", "content": "%s"}
                ],
                "stream": false,
                "temperature": 0.8
            }
            """,
                escapeJson(model),
                escapeJson(systemPrompt),
                escapeJson(userMessage));

        System.out.println(">>> [AI-Chat] 请求 URL: " + url + " | model: " + model);

        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build()) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(120))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            String body = response.body();

            if (status == 200) {
                // OpenAI 标准响应: choices[0].message.content
                String content = extractMessageContent(body);
                if (content != null && !content.isBlank()) {
                    return content;
                }
                System.err.println(">>> [AI-Chat] 解析响应失败,全文: " + body);
                return "AI 助手返回为空,请稍后再试。";
            } else {
                System.err.println(">>> [AI-Chat] HTTP " + status + ": " + body);
                if (status == 401) {
                    return "AI 助手认证失败,请检查 AI_API_KEY 是否正确。";
                }
                if (status == 429) {
                    return "AI 助手调用太频繁,请稍后再试。";
                }
                return "AI 助手繁忙 (HTTP " + status + ")";
            }
        } catch (HttpTimeoutException te) {
            System.err.println(">>> [AI-Chat] 调用超时: " + te.getMessage());
            return "AI 思考时间过长,请稍后再试。";
        } catch (Exception e) {
            System.err.println(">>> [AI-Chat] 调用异常: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return "AI 助手连接失败 (" + e.getMessage() + ")";
        }
    }

    /**
     * 极简 JSON 字符串转义(只处理常见字符,够用)。
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 从 OpenAI 兼容响应中提取 choices[0].message.content。
     * 不引入 JSON 库,手写简单解析以避免依赖。
     */
    private String extractMessageContent(String body) {
        if (body == null) return null;
        // 找 "content":"..." 这一段
        String marker = "\"content\":\"";
        int idx = body.indexOf(marker);
        if (idx < 0) return null;
        int start = idx + marker.length();
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < body.length(); i++) {
            char c = body.charAt(i);
            if (c == '\\' && i + 1 < body.length()) {
                char next = body.charAt(i + 1);
                switch (next) {
                    case 'n' -> { sb.append('\n'); i++; }
                    case 'r' -> { sb.append('\r'); i++; }
                    case 't' -> { sb.append('\t'); i++; }
                    case '"' -> { sb.append('"'); i++; }
                    case '\\' -> { sb.append('\\'); i++; }
                    case '/' -> { sb.append('/'); i++; }
                    case 'u' -> {
                        // 处理 unicode 转义 (反斜杠 u 加 4 位 hex)
                        if (i + 5 < body.length()) {
                            String hex = body.substring(i + 2, i + 6);
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 5;
                            } catch (NumberFormatException e) {
                                sb.append(c);
                            }
                        } else {
                            sb.append(c);
                        }
                    }
                    default -> sb.append(c);
                }
            } else if (c == '"') {
                return sb.toString();
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}