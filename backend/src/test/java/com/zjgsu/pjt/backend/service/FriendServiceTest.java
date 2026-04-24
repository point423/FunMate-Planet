package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.FriendRequest;
import com.zjgsu.pjt.backend.entity.Friendship;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.FriendRequestRepository;
import com.zjgsu.pjt.backend.repository.FriendshipRepository;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FriendServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private FriendService friendService;

    @Test
    @DisplayName("测试发送好友申请-成功")
    void sendFriendRequest_Success() {
        when(friendRequestRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(null);
        String result = friendService.sendFriendRequest(1L, 2L);
        assertEquals("好友申请已发送", result);
        verify(friendRequestRepository).save(any());
    }

    @Test
    @DisplayName("测试处理申请-接受并建立双向关系")
    void handleRequest_Accept() {
        FriendRequest req = new FriendRequest();
        req.setId(1L);
        req.setSenderId(1L);
        req.setReceiverId(2L);
        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(req));

        boolean success = friendService.handleRequest(1L, true);
        assertTrue(success);
        assertEquals("accepted", req.getStatus());
        verify(friendshipRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("测试获取好友列表")
    void getFriends_Success() {
        Friendship f = new Friendship();
        f.setFriendId(2L);
        when(friendshipRepository.findByUserId(eq(1L), any())).thenReturn(new PageImpl<>(Collections.singletonList(f)));
        when(userRepository.findAllById(any())).thenReturn(Collections.singletonList(new User()));

        List<User> friends = friendService.getFriends(1L);
        assertFalse(friends.isEmpty());
    }

    @Test
    @DisplayName("测试删除好友-解除双向关系")
    void deleteFriend_Success() {
        Friendship f1 = new Friendship();
        when(friendshipRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(f1);

        boolean success = friendService.deleteFriend(1L, 2L);
        assertTrue(success);
        verify(friendshipRepository, atLeastOnce()).delete(any());
    }
}
