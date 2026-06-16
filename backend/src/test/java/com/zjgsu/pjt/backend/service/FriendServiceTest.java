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

import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageImpl;

import org.springframework.data.domain.PageRequest;


import java.util.*;


import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.anyLong;

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


    // ============ getFriends ============

    @Test

    @DisplayName("getFriends-双向好友都存在,返回好友列表")

    void getFriends_Mutual() {

        Friendship f1 = new Friendship();

        f1.setUserId(1L);

        f1.setFriendId(2L);

        Friendship f1r = new Friendship();

        f1r.setUserId(2L);

        f1r.setFriendId(1L);


        Page<Friendship> page = new PageImpl<>(Arrays.asList(f1));

        when(friendshipRepository.findByUserId(eq(1L), any(PageRequest.class))).thenReturn(page);

        when(friendshipRepository.findAllByUserIdAndFriendId(2L, 1L)).thenReturn(Arrays.asList(f1r));


        User u2 = new User();

        u2.setId(2L);

        when(userRepository.findAllById(Arrays.asList(2L))).thenReturn(Arrays.asList(u2));


        List<User> result = friendService.getFriends(1L);

        assertEquals(1, result.size());

        assertEquals(2L, result.get(0).getId());

    }


    @Test

    @DisplayName("getFriends-单向(没互加),不返回")

    void getFriends_OneWay_Excluded() {

        Friendship f1 = new Friendship();

        f1.setUserId(1L);

        f1.setFriendId(2L);


        Page<Friendship> page = new PageImpl<>(Arrays.asList(f1));

        when(friendshipRepository.findByUserId(eq(1L), any(PageRequest.class))).thenReturn(page);

        when(friendshipRepository.findAllByUserIdAndFriendId(2L, 1L)).thenReturn(Arrays.asList());


        List<User> result = friendService.getFriends(1L);

        assertTrue(result.isEmpty());

    }


    @Test

    @DisplayName("getFriends-没有任何好友,返回空列表")

    void getFriends_Empty() {

        Page<Friendship> page = new PageImpl<>(Arrays.asList());

        when(friendshipRepository.findByUserId(eq(1L), any(PageRequest.class))).thenReturn(page);


        List<User> result = friendService.getFriends(1L);

        assertTrue(result.isEmpty());

    }


    // ============ getFriendById ============

    @Test

    @DisplayName("getFriendById-找到用户")

    void getFriendById_Found() {

        User u = new User();

        u.setId(2L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(u));


        User result = friendService.getFriendById(2L);

        assertNotNull(result);

        assertEquals(2L, result.getId());

    }


    @Test

    @DisplayName("getFriendById-没找到,返回null")

    void getFriendById_NotFound() {

        when(userRepository.findById(999L)).thenReturn(Optional.empty());


        User result = friendService.getFriendById(999L);

        assertNull(result);

    }


    // ============ deleteFriend ============

    @Test

    @DisplayName("deleteFriend-存在好友关系,删除成功")

    void deleteFriend_Success() {

        Friendship f = new Friendship();

        f.setUserId(1L);

        f.setFriendId(2L);

        Friendship fr = new Friendship();

        fr.setUserId(2L);

        fr.setFriendId(1L);


        when(friendshipRepository.findAllByUserIdAndFriendId(1L, 2L)).thenReturn(Arrays.asList(f));

        when(friendshipRepository.findAllByUserIdAndFriendId(2L, 1L)).thenReturn(Arrays.asList(fr));


        boolean result = friendService.deleteFriend(1L, 2L);

        assertTrue(result);

        verify(friendshipRepository, times(2)).deleteAll(anyList());

    }


    @Test

    @DisplayName("deleteFriend-不存在好友关系,返回false")

    void deleteFriend_NotExists() {

        when(friendshipRepository.findAllByUserIdAndFriendId(1L, 2L)).thenReturn(Arrays.asList());


        boolean result = friendService.deleteFriend(1L, 2L);

        assertFalse(result);

        verify(friendshipRepository, never()).deleteAll(anyList());

    }


    // ============ sendFriendRequest ============

    @Test

    @DisplayName("sendFriendRequest-新申请,发送成功")

    void sendFriendRequest_New() {

        when(friendRequestRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(null);

        when(friendRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));


        String result = friendService.sendFriendRequest(1L, 2L);

        assertEquals("好友申请已发送", result);

    }


    @Test

    @DisplayName("sendFriendRequest-已发过,提示已发送")

    void sendFriendRequest_Duplicate() {

        FriendRequest existing = new FriendRequest();

        when(friendRequestRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(existing);


        String result = friendService.sendFriendRequest(1L, 2L);

        assertEquals("已发送过申请", result);

        verify(friendRequestRepository, never()).save(any());

    }


    // ============ getRequests ============

    @Test

    @DisplayName("getRequests-返回收件+发件列表")

    void getRequests_ReturnsBoth() {

        FriendRequest in = new FriendRequest();

        FriendRequest out = new FriendRequest();

        when(friendRequestRepository.findByReceiverId(1L)).thenReturn(Arrays.asList(in));

        when(friendRequestRepository.findBySenderId(1L)).thenReturn(Arrays.asList(out));


        Map<String, Object> result = friendService.getRequests(1L);

        assertEquals(1, ((List) result.get("incoming")).size());

        assertEquals(1, ((List) result.get("outgoing")).size());

    }


    // ============ getRequestById ============

    @Test

    @DisplayName("getRequestById-发送者能查看")

    void getRequestById_AsSender() {

        FriendRequest req = new FriendRequest();

        req.setSenderId(1L);

        req.setReceiverId(2L);

        when(friendRequestRepository.findById(99L)).thenReturn(Optional.of(req));


        FriendRequest result = friendService.getRequestById(99L, 1L);

        assertNotNull(result);

    }


    @Test

    @DisplayName("getRequestById-无关人员不能查看,返回null")

    void getRequestById_NoPermission() {

        FriendRequest req = new FriendRequest();

        req.setSenderId(1L);

        req.setReceiverId(2L);

        when(friendRequestRepository.findById(99L)).thenReturn(Optional.of(req));


        FriendRequest result = friendService.getRequestById(99L, 999L);

        assertNull(result);

    }


    @Test

    @DisplayName("getRequestById-请求不存在,返回null")

    void getRequestById_NotFound() {

        when(friendRequestRepository.findById(999L)).thenReturn(Optional.empty());


        FriendRequest result = friendService.getRequestById(999L, 1L);

        assertNull(result);

    }


    // ============ handleRequest ============

    @Test

    @DisplayName("handleRequest-接收者接受,创建双向好友")

    void handleRequest_Accept() {

        FriendRequest req = new FriendRequest();

        req.setSenderId(1L);

        req.setReceiverId(2L);

        req.setStatus("pending");


        when(friendRequestRepository.findById(99L)).thenReturn(Optional.of(req));

        when(friendRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        when(friendshipRepository.findAllByUserIdAndFriendId(anyLong(), anyLong())).thenReturn(Arrays.asList());


        boolean result = friendService.handleRequest(99L, true, 2L);

        assertTrue(result);

        // 创建了 2 个方向的 Friendship

        verify(friendshipRepository, times(2)).save(any(Friendship.class));

    }


    @Test

    @DisplayName("handleRequest-非接收者操作,返回false")

    void handleRequest_NotReceiver() {

        FriendRequest req = new FriendRequest();

        req.setSenderId(1L);

        req.setReceiverId(2L);

        when(friendRequestRepository.findById(99L)).thenReturn(Optional.of(req));


        boolean result = friendService.handleRequest(99L, true, 999L);

        assertFalse(result);

    }


    @Test

    @DisplayName("handleRequest-拒绝,不创建好友")

    void handleRequest_Decline() {

        FriendRequest req = new FriendRequest();

        req.setSenderId(1L);

        req.setReceiverId(2L);

        req.setStatus("pending");


        when(friendRequestRepository.findById(99L)).thenReturn(Optional.of(req));

        when(friendRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));


        boolean result = friendService.handleRequest(99L, false, 2L);

        assertTrue(result);

        verify(friendshipRepository, never()).save(any(Friendship.class));

    }


    // ============ deleteRequest ============

    @Test

    @DisplayName("deleteRequest-发送者删除,成功")

    void deleteRequest_AsSender() {

        FriendRequest req = new FriendRequest();

        req.setSenderId(1L);

        req.setReceiverId(2L);

        when(friendRequestRepository.findById(99L)).thenReturn(Optional.of(req));


        boolean result = friendService.deleteRequest(99L, 1L);

        assertTrue(result);

        verify(friendRequestRepository).deleteById(99L);

    }


    @Test

    @DisplayName("deleteRequest-无关人员不能删,返回false")

    void deleteRequest_NoPermission() {

        FriendRequest req = new FriendRequest();

        req.setSenderId(1L);

        req.setReceiverId(2L);

        when(friendRequestRepository.findById(99L)).thenReturn(Optional.of(req));


        boolean result = friendService.deleteRequest(99L, 999L);

        assertFalse(result);

        verify(friendRequestRepository, never()).deleteById(any());

    }

}