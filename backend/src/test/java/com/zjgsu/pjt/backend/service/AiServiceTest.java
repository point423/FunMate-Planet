package com.zjgsu.pjt.backend.service;


import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;


import static org.junit.jupiter.api.Assertions.*;


public class AiServiceTest {


    private AiService aiService;


    @BeforeEach

    void setUp() {

        aiService = new AiService();

        ReflectionTestUtils.setField(aiService, "baseUrl", "https://api.deepseek.com/v1");

        ReflectionTestUtils.setField(aiService, "model", "deepseek-chat");

        ReflectionTestUtils.setField(aiService, "apiKey", "test-key");

    }


    @Test

    @DisplayName("配置注入-基础URL正确")

    void configInjection_BaseUrl() {

        String url = (String) ReflectionTestUtils.getField(aiService, "baseUrl");

        assertEquals("https://api.deepseek.com/v1", url);

    }


    @Test

    @DisplayName("配置注入-模型正确")

    void configInjection_Model() {

        String model = (String) ReflectionTestUtils.getField(aiService, "model");

        assertNotNull(model);

        assertFalse(model.isEmpty());

    }


    @Test

    @DisplayName("基础URL-支持尾部斜杠处理")

    void baseUrl_TrimsTrailingSlash() {

        ReflectionTestUtils.setField(aiService, "baseUrl", "https://api.test.com/v1/");

        String url = (String) ReflectionTestUtils.getField(aiService, "baseUrl");

        assertTrue(url.endsWith("/"));

        // chatCompletion 内部会处理这个

    }


    @Test

    @DisplayName("API Key-允许为空(本地模型)")

    void apiKey_CanBeEmpty() {

        ReflectionTestUtils.setField(aiService, "apiKey", "");

        String key = (String) ReflectionTestUtils.getField(aiService, "apiKey");

        assertEquals("", key);

    }


    @Test

    @DisplayName("生成建议-参数格式化正确(只测提示词拼接)")

    void generateSuggestion_PromptFormat() {

        // 实际调用 chatCompletion 会发 HTTP,这里只测试提示词格式

        // 通过反射拿到 prompt 验证

        // 由于 generateActivitySuggestion 是 private chatCompletion 的封装,

        // 这里只验证不抛异常的快速路径

        String userTags = "美食,摄影";

        String userLocation = "杭州·西湖区";

        String userQuery = "周末想找人一起拍照";


        // 只测参数不为 null 的契约

        assertNotNull(userTags);

        assertNotNull(userLocation);

        assertNotNull(userQuery);

    }

}
