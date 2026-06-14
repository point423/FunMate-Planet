package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.dto.UserProfileResponse;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.ActivityParticipantRepository;
import com.zjgsu.pjt.backend.repository.ActivityRepository;
import com.zjgsu.pjt.backend.repository.SharedJournalShowcaseRepository;
import com.zjgsu.pjt.backend.repository.UserEvaluationRepository;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.GeoOperations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private GeoOperations<String, String> geoOperations;

    @Mock
    private SharedJournalShowcaseRepository sharedJournalShowcaseRepository;

    @Mock
    private ActivityParticipantRepository activityParticipantRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserEvaluationRepository userEvaluationRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("1. 根据ID查询用户-成功场景")
    void findById_UserExists_ReturnsUser() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        mockEmptyProfileData();

        UserProfileResponse result = userService.findById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("2. 根据ID查询用户-不存在场景")
    void findById_UserNotExists_ReturnsNull() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertNull(userService.findById(99L));
    }

    @Test
    @DisplayName("3. 更新用户资料-成功场景")
    void updateProfile_Success() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setNickname("OldNick");

        User updateInfo = new User();
        updateNick("NewNick", updateInfo);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User result = userService.updateProfile(1L, updateInfo);
        assertEquals("NewNick", result.getNickname());
    }

    private void updateNick(String nick, User user) {
        user.setNickname(nick);
    }

    @Test
    @DisplayName("4. 删除用户-成功场景并清理Redis")
    void deleteUser_Exists_ReturnsTrue() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(stringRedisTemplate.opsForGeo()).thenReturn(geoOperations);

        boolean result = userService.deleteUser(1L);
        
        assertTrue(result);
        verify(userRepository).deleteById(1L);
        verify(geoOperations).remove(anyString(), eq("1"));
    }

    @Test
    @DisplayName("5. 根据用户名查询-成功场景")
    void findByUsername_Exists() {
        User user = new User();
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername("testuser");
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    @DisplayName("6. 查询所有用户-列表不为空")
    void getAllUsers_ReturnsList() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(new User()));
        mockEmptyProfileData();

        List<UserProfileResponse> result = userService.getAllUsers();
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("7. 更新地理位置-触发数据库和Redis双写")
    void updateLocation_TriggersDoubleWrite() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(stringRedisTemplate.opsForGeo()).thenReturn(geoOperations);

        userService.updateLocation(1L, 120.0, 30.0);

        verify(userRepository).save(user);
        assertEquals(120.0, user.getLongitude());
        verify(geoOperations).add(anyString(), any(), eq("1"));
    }

    @Test
    @DisplayName("8. 更新资料-用户不存在则返回空")
    void updateProfile_UserNotFound_ReturnsNull() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        User result = userService.updateProfile(1L, new User());
        assertNull(result);
    }

    private void mockEmptyProfileData() {
        when(userEvaluationRepository.findByTargetId(any())).thenReturn(Collections.emptyList());
        when(sharedJournalShowcaseRepository.findByUserIdOrderByCreateTimeDesc(any())).thenReturn(Collections.emptyList());
        when(activityParticipantRepository.findByUserId(any())).thenReturn(Collections.emptyList());
        when(activityRepository.findByIdIn(any())).thenReturn(Collections.emptyList());
    }
}
