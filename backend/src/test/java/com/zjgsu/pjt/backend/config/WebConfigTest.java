package com.zjgsu.pjt.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * WebConfig 测试
 */
public class WebConfigTest {

    private WebConfig webConfig;
    private AuthInterceptor authInterceptor;
    private InterceptorRegistry interceptorRegistry;
    private CorsRegistry corsRegistry;
    private ResourceHandlerRegistry resourceHandlerRegistry;

    @BeforeEach
    void setUp() {
        authInterceptor = mock(AuthInterceptor.class);
        webConfig = new WebConfig();
        ReflectionTestUtils.setField(webConfig, "uploadDir", "uploads/");
        ReflectionTestUtils.setField(webConfig, "allowedOrigins", "*");
        ReflectionTestUtils.setField(webConfig, "authInterceptor", authInterceptor);

        interceptorRegistry = mock(InterceptorRegistry.class);
        corsRegistry = mock(CorsRegistry.class);
        resourceHandlerRegistry = mock(ResourceHandlerRegistry.class);

        // 链式 mock:registry.addInterceptor().addPathPatterns().excludePathPatterns()
        InterceptorRegistration interceptorReg = mock(InterceptorRegistration.class);
        when(interceptorRegistry.addInterceptor(any())).thenReturn(interceptorReg);
        when(interceptorReg.addPathPatterns(any(String[].class))).thenReturn(interceptorReg);
        when(interceptorReg.excludePathPatterns(any(String[].class))).thenReturn(interceptorReg);

        // 链式 mock:registry.addMapping().allowedOriginPatterns()...
        CorsRegistration corsReg = mock(CorsRegistration.class);
        when(corsRegistry.addMapping(any())).thenReturn(corsReg);
        when(corsReg.allowedOriginPatterns(any(String[].class))).thenReturn(corsReg);
        when(corsReg.allowedMethods(any(String[].class))).thenReturn(corsReg);
        when(corsReg.allowedHeaders(any())).thenReturn(corsReg);
        when(corsReg.exposedHeaders(any())).thenReturn(corsReg);
        when(corsReg.allowCredentials(anyBoolean())).thenReturn(corsReg);
        when(corsReg.maxAge(anyLong())).thenReturn(corsReg);

        ResourceHandlerRegistration resourceReg = mock(ResourceHandlerRegistration.class);
        when(resourceHandlerRegistry.addResourceHandler(any(String[].class))).thenReturn(resourceReg);
        when(resourceReg.addResourceLocations(any(String[].class))).thenReturn(resourceReg);
    }

    @Test
    @DisplayName("addInterceptors-注册了 AuthInterceptor")
    void addInterceptors_RegistersAuth() {
        webConfig.addInterceptors(interceptorRegistry);
        verify(interceptorRegistry).addInterceptor(authInterceptor);
    }

    @Test
    @DisplayName("addCorsMappings-默认 CORS(*)")
    void addCorsMappings_DefaultWildcard() {
        webConfig.addCorsMappings(corsRegistry);
        verify(corsRegistry).addMapping("/api/**");
    }

    @Test
    @DisplayName("addCorsMappings-多个 origin")
    void addCorsMappings_MultipleOrigins() {
        ReflectionTestUtils.setField(webConfig, "allowedOrigins", "http://a.com,http://b.com");
        webConfig.addCorsMappings(corsRegistry);
        verify(corsRegistry).addMapping("/api/**");
    }

    @Test
    @DisplayName("addCorsMappings-空字符串 fallback 到 *")
    void addCorsMappings_EmptyOrigins() {
        ReflectionTestUtils.setField(webConfig, "allowedOrigins", "");
        webConfig.addCorsMappings(corsRegistry);
        verify(corsRegistry).addMapping("/api/**");
    }

    @Test
    @DisplayName("addResourceHandlers-配置 /static/** 资源")
    void addResourceHandlers_Static() {
        webConfig.addResourceHandlers(resourceHandlerRegistry);
        verify(resourceHandlerRegistry).addResourceHandler("/static/**");
    }
}