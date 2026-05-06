package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.geo.Circle;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DiscoverServiceTest { // ✅ 修复：类名必须与文件名一致

    @Mock
    private UserRepository userRepository;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private GeoOperations<String, String> geoOperations;

    @InjectMocks
    private DiscoverService discoverService;

    @Test
    @DisplayName("测试附近的人查询-成功获取列表")
    void getNearbyUsers_Success() {
        when(stringRedisTemplate.opsForGeo()).thenReturn(geoOperations);
        when(geoOperations.radius(anyString(), any(Circle.class))).thenReturn(null);

        List<User> result = discoverService.getNearbyUsers(120.0, 30.0, 10.0);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试排行榜查询-成功")
    void getRanking_Success() {
        when(userRepository.findAll(any(org.springframework.data.domain.PageRequest.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(Collections.singletonList(new User())));

        List<User> result = discoverService.getRanking(10);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("测试随机匹配用户")
    void getRandomUser_Success() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

        User result = discoverService.getRandomUser();
        assertNotNull(result);
    }
}
