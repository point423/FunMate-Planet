package com.zjgsu.pjt.backend.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AiService {

    @Value("${ai.api-key:ollama}")
    private String apiKey;

    @Value("${ai.base-url:http://ollama:11434/v1}")
    private String baseUrl;

    @Value("${ai.model:qwen2.5:1.5b}")
    private String model;

    public String generateActivitySuggestion(String userTags, String userLocation, String userQuery) {
        System.out.println(">>> 准备调用 AI 服务. URL: " + baseUrl + ", 模型: " + model);
        try {
            OpenAIClient client = OpenAIOkHttpClient.builder()
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .timeout(Duration.ofMinutes(3)) // 增加到 3 分钟，Ollama 第一次启动很慢
                    .build();

            String prompt = String.format(
                    "你是一个社交星球的活动助手。用户信息：标签[%s]，位置[%s]。用户请求：%s。请给出幽默风趣的活动建议，字数150字内。",
                    userTags, userLocation, userQuery
            );

            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(model)
                    .addUserMessage(prompt)
                    .temperature(0.7)
                    .build();

            ChatCompletion completion = client.chat().completions().create(params);
            String result = completion.choices().get(0).message().content().orElse("AI 正在思考中...");
            System.out.println(">>> AI 回复成功: " + result);
            return result;
        } catch (Exception e) {
            System.err.println(">>> AI 调用失败: " + e.getMessage());
            e.printStackTrace();
            return "抱歉，AI 助手暂时走开了 (" + e.getMessage() + ")";
        }
    }
}
