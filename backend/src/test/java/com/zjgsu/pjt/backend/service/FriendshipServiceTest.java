package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.Friendship;
import com.zjgsu.pjt.backend.repository.FriendshipRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private FriendshipService friendshipService;

    @Test
    @DisplayName("测试关注用户-成功")
    void follow_New_Success() {
        when(friendshipRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(null);
        String result = friendshipService.follow(1L, 2L, true);
        assertEquals("关注成功", result);
        verify(friendshipRepository).save(any(Friendship.class));
    }

    @Test
    @DisplayName("测试取消关注")
    void unfollow_Success() {
        Friendship f = new Friendship();
        when(friendshipRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(f);
        String result = friendshipService.follow(1L, 2L, false);
        assertEquals("已取消关注", result);
        verify(friendshipRepository).delete(f);
    }

    @Test
    @DisplayName("测试物理删除关系-成功场景")
    void deleteFriendship_Success() {
        Friendship f = new Friendship();
        f.setId(1L);
        f.setUserId(1L);
        when(friendshipRepository.findById(1L)).thenReturn(Optional.of(f));

        // ✅ 核心修复：传入 2 个参数
        boolean result = friendshipService.deleteFriendship(1L, 1L);
        assertTrue(result);
        verify(friendshipRepository).deleteById(1L);
    }

    @Test
    @DisplayName("安全校验-越权删除应返回false")
    void deleteFriendship_Forbidden() {
        Friendship f = new Friendship();
        f.setId(1L);
        f.setUserId(99L);
        when(friendshipRepository.findById(1L)).thenReturn(Optional.of(f));

        // 用户 1 尝试删除属于用户 99 的关系
        boolean result = friendshipService.deleteFriendship(1L, 1L);
        assertFalse(result);
    }
}
