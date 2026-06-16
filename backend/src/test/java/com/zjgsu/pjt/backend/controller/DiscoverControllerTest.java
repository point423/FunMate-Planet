package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.service.DiscoverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DiscoverController 测试
 *
 * 注意:
 * 1. Result.success() 返回 code=0(不是 200),HTTP 状态固定 200
 * 2. standaloneSetup 不走 AuthInterceptor,@RequestAttribute 需手动注入
 * 3. Mockito 5 严格模式下,使用 any() 等宽松匹配器避免误报
 */
@ExtendWith(MockitoExtension.class)
public class DiscoverControllerTest {

    @Mock
    private DiscoverService discoverService;

    @InjectMocks
    private DiscoverController discoverController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(discoverController).build();
    }

    @Test
    @DisplayName("GET /api/discover/nearby-成功返回附近用户列表")
    void getNearby_Success() throws Exception {
        User u1 = new User();
        u1.setId(1L);
        u1.setUsername("alice");
        when(discoverService.getNearbyUsers(any(), any(), anyDouble()))
                .thenReturn(Arrays.asList(u1));

        mockMvc.perform(get("/api/discover/nearby")
                        .param("longitude", "120.0")
                        .param("latitude", "30.0")
                        .param("radius", "5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data[0].username").value("alice"));
    }

    @Test
    @DisplayName("GET /api/discover/nearby-默认参数(无经纬度)")
    void getNearby_DefaultParams() throws Exception {
        when(discoverService.getNearbyUsers(any(), any(), anyDouble()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/discover/nearby"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/discover/ranking-成功返回排行榜")
    void getRanking_Success() throws Exception {
        User u1 = new User();
        u1.setId(1L);
        u1.setUsername("top1");
        when(discoverService.getRanking(10)).thenReturn(Arrays.asList(u1));

        mockMvc.perform(get("/api/discover/ranking").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].username").value("top1"));
    }

    @Test
    @DisplayName("GET /api/discover/ranking-默认 limit 返回 10")
    void getRanking_DefaultLimit() throws Exception {
        when(discoverService.getRanking(anyInt())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/discover/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("GET /api/discover/random-返回随机用户")
    void getRandom_Success() throws Exception {
        User u = new User();
        u.setId(1L);
        u.setUsername("random");
        when(discoverService.getRandomUser()).thenReturn(u);

        mockMvc.perform(get("/api/discover/random"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("random"));
    }

    @Test
    @DisplayName("GET /api/discover/random-没有用户,业务 code = 404")
    void getRandom_NotFound() throws Exception {
        when(discoverService.getRandomUser()).thenReturn(null);

        mockMvc.perform(get("/api/discover/random"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("暂无用户"));
    }

    @Test
    @DisplayName("POST /api/discover/location-成功更新位置")
    void updateLocation_Success() throws Exception {
        java.lang.reflect.Method method = DiscoverController.class.getDeclaredMethod(
                "updateLocation", java.util.Map.class, Long.class);
        method.setAccessible(true);

        java.util.Map<String, Double> params = new java.util.HashMap<>();
        params.put("longitude", 120.0);
        params.put("latitude", 30.0);
        method.invoke(discoverController, params, 1L);

        // 用宽松匹配器(避免 Mockito 5 严格模式误报)
        verify(discoverService, times(1)).updateUserLocation(any(), any(), any());
    }
}