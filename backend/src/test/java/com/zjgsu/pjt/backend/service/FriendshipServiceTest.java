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
    @DisplayName("测试关注用户-新关注成功")
    void follow_New_Success() {
        when(friendshipRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(null);

        String result = friendshipService.follow(1L, 2L, true);

        assertEquals("关注成功", result);
        verify(friendshipRepository).save(any(Friendship.class));
    }

    @Test
    @DisplayName("测试关注用户-重复关注（覆盖分支）")
    void follow_AlreadyFollowed() {
        when(friendshipRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(new Friendship());

        String result = friendshipService.follow(1L, 2L, true);

        assertEquals("关注成功", result);
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    @DisplayName("测试取消关注-成功")
    void unfollow_Success() {
        Friendship f = new Friendship();
        when(friendshipRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(f);

        String result = friendshipService.follow(1L, 2L, false);

        assertEquals("已取消关注", result);
        verify(friendshipRepository).delete(f);
    }

    @Test
    @DisplayName("测试获取粉丝/关注列表")
    void getList_Success() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(friendshipRepository.findByFriendId(anyLong(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));
        when(friendshipRepository.findByUserId(anyLong(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        assertNotNull(friendshipService.getFollowers(1L, pageable));
        assertNotNull(friendshipService.getFollowing(1L, pageable));
    }

    @Test
    @DisplayName("测试删除社交关系")
    void deleteFriendship_Success() {
        when(friendshipRepository.existsById(1L)).thenReturn(true);
        boolean result = friendshipService.deleteFriendship(1L);
        assertTrue(result);
        verify(friendshipRepository).deleteById(1L);
    }
}
