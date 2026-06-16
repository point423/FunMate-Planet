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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        List<User> result = discoverService.getNearbyUsers(120.0, 30.0, 10.0);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Redis Geo 鍛戒腑鏃惰繑鍥炲搴旂敤鎴�")
    void getNearbyUsers_ReturnsUsersFromRedisGeoResults() {
        User user = new User();
        user.setId(1L);
        GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult = new GeoResult<>(
                new RedisGeoCommands.GeoLocation<>("1", new Point(120.0, 30.0)),
                new Distance(1.0)
        );
        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = new GeoResults<>(List.of(geoResult));

        when(stringRedisTemplate.opsForGeo()).thenReturn(geoOperations);
        when(geoOperations.radius(anyString(), any(Circle.class))).thenReturn(geoResults);
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(user));

        List<User> result = discoverService.getNearbyUsers(120.0, 30.0, 10.0);

        assertEquals(List.of(user), result);
    }

    @Test
    @DisplayName("测试排行榜查询-成功")
    void getRanking_Success() {
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(new User())));

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

    @Test
    @DisplayName("娌℃湁鐢ㄦ埛鏃堕殢鏈哄尮閰嶈繑鍥瀗ull")
    void getRandomUser_ReturnsNullWhenRepositoryEmpty() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        assertNull(discoverService.getRandomUser());
    }

    @Test
    @DisplayName("涓婃姤浣嶇疆鏃跺啓鍏edis Geo")
    void updateUserLocation_WritesRedisGeoPoint() {
        when(stringRedisTemplate.opsForGeo()).thenReturn(geoOperations);

        discoverService.updateUserLocation(1L, 120.0, 30.0);

        verify(geoOperations).add(eq("user:location"), any(Point.class), eq("1"));
    }

    @Test
    @DisplayName("浣嶇疆鍙傛暟涓虹┖鏃朵笉鍐欏叆Redis")
    void updateUserLocation_IgnoresMissingCoordinates() {
        discoverService.updateUserLocation(1L, null, 30.0);
        discoverService.updateUserLocation(1L, 120.0, null);

        verify(stringRedisTemplate, org.mockito.Mockito.never()).opsForGeo();
    }

    @Test
    @DisplayName("鍒犻櫎Redis浣嶇疆杩斿洖鎴愬姛鐘舵€�")
    void deleteUserLocation_ReturnsTrueWhenRedisRemovesKey() {
        when(stringRedisTemplate.opsForGeo()).thenReturn(geoOperations);
        when(geoOperations.remove("user:location", "1")).thenReturn(1L);

        assertTrue(discoverService.deleteUserLocation(1L));
    }
}
