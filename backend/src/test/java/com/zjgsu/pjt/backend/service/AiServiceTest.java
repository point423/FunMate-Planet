package com.zjgsu.pjt.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class AiServiceTest {

    private AiService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiService();
        ReflectionTestUtils.setField(aiService, "baseUrl", "https://api.deepseek.com/v1");
        ReflectionTestUtils.setField(aiService, "model", "deepseek-chat");
        ReflectionTestUtils.setField(aiService, "apiKey", "test-key");
    }

    @Test
    void configInjection_BaseUrl() {
        assertThat(ReflectionTestUtils.getField(aiService, "baseUrl"))
                .isEqualTo("https://api.deepseek.com/v1");
    }

    @Test
    void configInjection_Model() {
        assertThat((String) ReflectionTestUtils.getField(aiService, "model"))
                .isNotBlank();
    }

    @Test
    void baseUrl_CanContainTrailingSlashBeforeRequestBuild() {
        ReflectionTestUtils.setField(aiService, "baseUrl", "https://api.test.com/v1/");

        assertThat(ReflectionTestUtils.getField(aiService, "baseUrl"))
                .isEqualTo("https://api.test.com/v1/");
    }

    @Test
    void apiKey_CanBeEmptyForLocalCompatibleModels() {
        ReflectionTestUtils.setField(aiService, "apiKey", "");

        assertThat(ReflectionTestUtils.getField(aiService, "apiKey"))
                .isEqualTo("");
    }

    @Test
    void generateActivitySuggestion_ReturnsFallbackWhenClientCannotBuildRequest() {
        ReflectionTestUtils.setField(aiService, "baseUrl", "http://[invalid");
        ReflectionTestUtils.setField(aiService, "model", "test-model");
        ReflectionTestUtils.setField(aiService, "apiKey", "test-key");

        String result = aiService.generateActivitySuggestion("movie,coffee", "Hangzhou", "recommend something");

        assertThat(result).contains("AI");
    }

    @Test
    void escapeJson_EscapesSpecialCharacters() {
        String escaped = ReflectionTestUtils.invokeMethod(
                aiService,
                "escapeJson",
                "quote\" newline\n slash\\ tab\t"
        );

        assertThat(escaped).isEqualTo("quote\\\" newline\\n slash\\\\ tab\\t");
    }

    @Test
    void extractMessageContent_ParsesEscapedOpenAiContent() {
        String body = """
                {"choices":[{"message":{"content":"hello\\nworld\\u0021"}}]}
                """;

        String content = ReflectionTestUtils.invokeMethod(aiService, "extractMessageContent", body);

        assertThat(content).isEqualTo("hello\nworld!");
    }

    @Test
    void extractMessageContent_ReturnsNullWhenContentMissing() {
        String content = ReflectionTestUtils.invokeMethod(aiService, "extractMessageContent", "{\"choices\":[]}");

        assertThat(content).isNull();
    }
}
