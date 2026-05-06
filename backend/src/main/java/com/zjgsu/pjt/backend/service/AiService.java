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

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.base-url:https://api.deepseek.com/v1}")
    private String baseUrl;

    @Value("${ai.model:qwen2.5:1.5b}")
    private String model;

    public String generateActivitySuggestion(String userTags, String userLocation, String userQuery) {
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .timeout(Duration.ofMinutes(5))
                .build();

        String prompt = String.format(
                "你是一个活动推荐助手。用户信息：兴趣标签[%s]，位置[%s]。用户问题：%s。请给出个性化的活动建议和搭子匹配建议，用中文回答，简洁明了。",
                userTags, userLocation, userQuery
        );

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(model)
                .addUserMessage(prompt)
                .maxTokens(500)
                .temperature(0.7)
                .build();

        ChatCompletion completion = client.chat().completions().create(params);
        return completion.choices().get(0).message().content().orElse("抱歉，暂时无法生成建议");
    }
}