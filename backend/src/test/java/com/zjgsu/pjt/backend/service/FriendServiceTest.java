package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.FriendRequest;
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

import java.util.Collections;
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
    @DisplayName("测试处理申请-成功场景")
    void handleRequest_Accept() {
        FriendRequest req = new FriendRequest();
        req.setId(1L);
        req.setSenderId(1L);
        req.setReceiverId(2L);
        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(req));

        // ✅ 核心修复：传入 3 个参数 (requestId, accept, currentUserId)
        boolean success = friendService.handleRequest(1L, true, 2L);
        assertTrue(success);
        verify(friendshipRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("测试删除申请-成功场景")
    void deleteRequest_Success() {
        FriendRequest req = new FriendRequest();
        req.setSenderId(1L);
        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(req));

        // ✅ 核心修复：传入 2 个参数
        boolean success = friendService.deleteRequest(1L, 1L);
        assertTrue(success);
        verify(friendRequestRepository).deleteById(1L);
    }
}
