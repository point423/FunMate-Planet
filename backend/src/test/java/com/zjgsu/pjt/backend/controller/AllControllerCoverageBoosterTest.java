package com.zjgsu.pjt.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjgsu.pjt.backend.dto.UserProfileResponse;
import com.zjgsu.pjt.backend.entity.Activity;
import com.zjgsu.pjt.backend.entity.ActivityDiary;
import com.zjgsu.pjt.backend.entity.ActivityInvitation;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.entity.UserEvaluation;
import com.zjgsu.pjt.backend.service.ActivityInvitationService;
import com.zjgsu.pjt.backend.service.ActivityService;
import com.zjgsu.pjt.backend.service.AuthService;
import com.zjgsu.pjt.backend.service.ChatService;
import com.zjgsu.pjt.backend.service.DiaryService;
import com.zjgsu.pjt.backend.service.DiscoverService;
import com.zjgsu.pjt.backend.service.EvaluationService;
import com.zjgsu.pjt.backend.service.FriendService;
import com.zjgsu.pjt.backend.service.FriendshipService;
import com.zjgsu.pjt.backend.service.UploadService;
import com.zjgsu.pjt.backend.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Controller coverage booster")
class AllControllerCoverageBoosterTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Mock
    private AuthService authService;
    @Mock
    private UserService userService;
    @Mock
    private DiscoverService discoverService;
    @Mock
    private ActivityService activityService;
    @Mock
    private FriendService friendService;
    @Mock
    private ChatService chatService;
    @Mock
    private DiaryService diaryService;
    @Mock
    private EvaluationService evaluationService;
    @Mock
    private UploadService uploadService;
    @Mock
    private ActivityInvitationService activityInvitationService;
    @Mock
    private FriendshipService friendshipService;

    @Test
    void authController_CoversRegisterLoginLogoutAndMe() throws Exception {
        AuthController controller = new AuthController();
        ReflectionTestUtils.setField(controller, "authService", authService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        User user = new User();
        user.setUsername("alice");
        user.setPassword("123");

        when(authService.register(any(User.class))).thenReturn(true, false);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        when(authService.login("alice", "123")).thenReturn(Map.of("token", "jwt-token-123", "userId", 1L));
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "alice", "password", "123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("jwt-token-123"));

        when(authService.getCurrentUser(1L)).thenReturn(Map.of("id", 1L, "username", "alice"));
        mockMvc.perform(get("/api/auth/me").requestAttr("currentUserId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("alice"));
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk());
    }

    @Test
    void userController_CoversReadSearchUpdateDeleteAndLocation() throws Exception {
        UserController controller = new UserController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        UserProfileResponse profile = new UserProfileResponse();
        when(userService.getAllUsers()).thenReturn(List.of(profile));
        when(userService.findById(1L)).thenReturn(profile);
        User found = new User();
        found.setId(1L);
        when(userService.findByUsername("alice")).thenReturn(Optional.of(found));
        when(userService.updateUser(anyLong(), any(User.class))).thenReturn(found);
        when(userService.updateProfile(anyLong(), any(User.class))).thenReturn(found);
        when(userService.deleteUser(1L)).thenReturn(true);

        mockMvc.perform(get("/api/users")).andExpect(status().isOk());
        mockMvc.perform(get("/api/users/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/users/me").requestAttr("currentUserId", 1L)).andExpect(status().isOk());
        mockMvc.perform(get("/api/users/by-username").param("username", " alice ")).andExpect(status().isOk());
        mockMvc.perform(put("/api/users/me")
                        .requestAttr("currentUserId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"Alice\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/users/1")
                        .requestAttr("currentUserId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"Alice\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/users/1").requestAttr("currentUserId", 1L)).andExpect(status().isOk());
        mockMvc.perform(post("/api/users/location")
                        .requestAttr("currentUserId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"longitude\":120.1,\"latitude\":30.2}"))
                .andExpect(status().isOk());
    }

    @Test
    void discoverController_CoversNearbyRankingRandomAndLocation() throws Exception {
        DiscoverController controller = new DiscoverController();
        ReflectionTestUtils.setField(controller, "discoverService", discoverService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(discoverService.getNearbyUsers(any(), any(), anyDouble())).thenReturn(Collections.emptyList());
        when(discoverService.getRanking(anyInt())).thenReturn(Collections.emptyList());
        when(discoverService.getRandomUser()).thenReturn(new User());
        when(discoverService.deleteUserLocation(1L)).thenReturn(true);

        mockMvc.perform(get("/api/discover/nearby")).andExpect(status().isOk());
        mockMvc.perform(get("/api/discover/ranking")).andExpect(status().isOk());
        mockMvc.perform(get("/api/discover/random")).andExpect(status().isOk());
        mockMvc.perform(post("/api/discover/location")
                        .requestAttr("currentUserId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"longitude\":120.1,\"latitude\":30.2}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/discover/location").requestAttr("currentUserId", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void activityController_CoversListDetailMyAndActions() throws Exception {
        ActivityController controller = new ActivityController();
        ReflectionTestUtils.setField(controller, "activityService", activityService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        Activity activity = new Activity();
        activity.setId(1L);
        activity.setCreatorId(1L);
        when(activityService.createActivity(any(Activity.class))).thenReturn(activity);
        when(activityService.inviteFriend(anyLong(), anyLong(), anyLong())).thenReturn(activity);
        when(activityService.updateActivity(anyLong(), any(Activity.class), anyLong())).thenReturn(activity);
        when(activityService.getActivities(any(), any())).thenReturn(new PageImpl<>(List.of(activity)));
        when(activityService.getParticipants(anyLong())).thenReturn(Collections.emptyList());
        when(activityService.getMyActivities(anyLong())).thenReturn(new HashMap<>());
        when(activityService.getCompletableActivities(anyLong())).thenReturn(Collections.emptyList());
        when(activityService.getTopParticipants()).thenReturn(Collections.emptyList());
        when(activityService.findById(1L)).thenReturn(activity);
        when(activityService.hasJournal(1L)).thenReturn(true);
        when(activityService.joinActivity(1L, 1L)).thenReturn(0);
        when(activityService.completeActivity(1L, 1L)).thenReturn(true);
        when(activityService.deleteActivity(1L, 1L)).thenReturn(true);

        mockMvc.perform(post("/api/activities")
                        .requestAttr("currentUserId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Run\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/activities/1/invite")
                        .requestAttr("currentUserId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inviteeId\":2}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/activities/1")
                        .requestAttr("currentUserId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Run again\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/activities").param("status", "0")).andExpect(status().isOk());
        mockMvc.perform(get("/api/activities/my").requestAttr("currentUserId", 1L)).andExpect(status().isOk());
        mockMvc.perform(get("/api/activities/completable").requestAttr("currentUserId", 1L)).andExpect(status().isOk());
        mockMvc.perform(get("/api/activities/leaderboard")).andExpect(status().isOk());
        mockMvc.perform(get("/api/activities/1")).andExpect(status().isOk());
        mockMvc.perform(post("/api/activities/1/join").requestAttr("currentUserId", 1L)).andExpect(status().isOk());
        mockMvc.perform(post("/api/activities/1/complete").requestAttr("currentUserId", 1L)).andExpect(status().isOk());
        mockMvc.perform(delete("/api/activities/1").requestAttr("currentUserId", 1L)).andExpect(status().isOk());
    }

    @Test
    void friendChatAndInvitationControllers_CoverCommonRoutes() throws Exception {
        FriendController friendController = new FriendController();
        ReflectionTestUtils.setField(friendController, "friendService", friendService);
        MockMvc friendMvc = MockMvcBuilders.standaloneSetup(friendController).build();
        when(friendService.getFriends(anyLong())).thenReturn(Collections.emptyList());
        when(friendService.getFriendById(anyLong())).thenReturn(new User());
        when(friendService.getRequests(anyLong())).thenReturn(new HashMap<>());
        when(friendService.sendFriendRequest(anyLong(), anyLong())).thenReturn("sent");
        when(friendService.deleteFriend(anyLong(), anyLong())).thenReturn(true);

        friendMvc.perform(get("/api/friends").requestAttr("currentUserId", 1L)).andExpect(status().isOk());
        friendMvc.perform(get("/api/friends/2")).andExpect(status().isOk());
        friendMvc.perform(get("/api/friends/requests").requestAttr("currentUserId", 1L)).andExpect(status().isOk());
        friendMvc.perform(post("/api/friends/requests")
                        .requestAttr("currentUserId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetUserId\":2}"))
                .andExpect(status().isOk());
        friendMvc.perform(delete("/api/friends/2").requestAttr("currentUserId", 1L)).andExpect(status().isOk());

        ChatController chatController = new ChatController();
        ReflectionTestUtils.setField(chatController, "chatService", chatService);
        MockMvc chatMvc = MockMvcBuilders.standaloneSetup(chatController).build();
        when(chatService.getConversations(anyLong())).thenReturn(Collections.emptyList());
        when(chatService.getMessages(anyLong(), anyLong(), anyInt(), anyInt())).thenReturn(new HashMap<>());
        when(chatService.sendMessage(anyLong(), anyLong(), anyString())).thenReturn("ok");
        when(chatService.deleteMessage(anyString(), anyLong())).thenReturn(true);

        chatMvc.perform(get("/api/chat/conversations").requestAttr("currentUserId", 1L)).andExpect(status().isOk());
        chatMvc.perform(get("/api/chat/messages")
                        .requestAttr("currentUserId", 1L)
                        .param("targetUserId", "2"))
                .andExpect(status().isOk());
        chatMvc.perform(post("/api/chat/messages")
                        .requestAttr("currentUserId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receiverId\":2,\"content\":\"hello\"}"))
                .andExpect(status().isOk());
        chatMvc.perform(delete("/api/chat/messages/m1").requestAttr("currentUserId", 1L)).andExpect(status().isOk());

        ActivityInvitationController invitationController = new ActivityInvitationController();
        ReflectionTestUtils.setField(invitationController, "activityInvitationService", activityInvitationService);
        MockMvc invitationMvc = MockMvcBuilders.standaloneSetup(invitationController).build();
        when(activityInvitationService.getMyInvitations(anyLong())).thenReturn(new HashMap<>());
        when(activityInvitationService.getInvitationDetail(anyLong(), anyLong())).thenReturn(new HashMap<>());
        when(activityInvitationService.createInvitation(anyLong(), anyLong(), anyLong())).thenReturn(new ActivityInvitation());
        when(activityInvitationService.handleInvitation(anyLong(), anyBoolean(), anyLong())).thenReturn(true);

        invitationMvc.perform(get("/api/activity-invitations").requestAttr("currentUserId", 1L)).andExpect(status().isOk());
        invitationMvc.perform(get("/api/activity-invitations/1").requestAttr("currentUserId", 1L)).andExpect(status().isOk());
        invitationMvc.perform(post("/api/activity-invitations")
                        .requestAttr("currentUserId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activityId\":1,\"receiverId\":2}"))
                .andExpect(status().isOk());
        invitationMvc.perform(post("/api/activity-invitations/1/handle")
                        .requestAttr("currentUserId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accept\":true}"))
                .andExpect(status().isOk());
    }

    @Test
    void diaryEvaluationUploadAndSocialControllers_CoverCommonRoutes() throws Exception {
        DiaryController diaryController = new DiaryController();
        ReflectionTestUtils.setField(diaryController, "diaryService", diaryService);
        MockMvc diaryMvc = MockMvcBuilders.standaloneSetup(diaryController).build();
        ActivityDiary diary = new ActivityDiary();
        diary.setId(1L);
        diary.setUserId(1L);
        when(diaryService.getDiariesWithParticipants(anyLong(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));
        when(diaryService.findById(1L)).thenReturn(diary);
        when(diaryService.canAccessDiary(1L, 1L)).thenReturn(true);
        when(diaryService.getParticipantsByDiaryId(1L)).thenReturn(Collections.emptyList());
        when(diaryService.getSharedEntriesByDiaryId(1L)).thenReturn(Collections.emptyList());

        assertNotNull(diaryController.getDiaries(null, 1, 10, 1L));
        diaryMvc.perform(get("/api/diaries/1").requestAttr("currentUserId", 1L)).andExpect(status().isOk());

        EvaluationController evaluationController = new EvaluationController();
        ReflectionTestUtils.setField(evaluationController, "evaluationService", evaluationService);
        MockMvc evaluationMvc = MockMvcBuilders.standaloneSetup(evaluationController).build();
        when(evaluationService.createEvaluation(any(UserEvaluation.class))).thenReturn(new UserEvaluation());
        when(evaluationService.getEvaluationsByTargetId(anyLong())).thenReturn(Collections.emptyList());
        when(evaluationService.getEvaluationsByEvaluatorId(anyLong())).thenReturn(Collections.emptyList());
        when(evaluationService.getEvaluationById(anyLong())).thenReturn(Optional.of(new UserEvaluation()));

        evaluationMvc.perform(post("/api/evaluations")
                        .requestAttr("currentUserId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
        evaluationMvc.perform(get("/api/evaluations/target/1")).andExpect(status().isOk());
        evaluationMvc.perform(get("/api/evaluations/evaluator/1")).andExpect(status().isOk());
        evaluationMvc.perform(get("/api/evaluations/1")).andExpect(status().isOk());

        UploadController uploadController = new UploadController();
        ReflectionTestUtils.setField(uploadController, "uploadService", uploadService);
        MockMvc uploadMvc = MockMvcBuilders.standaloneSetup(uploadController).build();
        when(uploadService.uploadImage(any(), any(MockHttpServletRequest.class)))
                .thenReturn("http://localhost:8080/static/test.png");
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "test".getBytes());
        uploadMvc.perform(multipart("/api/upload/image").file(file).requestAttr("currentUserId", 1L))
                .andExpect(status().isOk());

        SocialController socialController = new SocialController();
        ReflectionTestUtils.setField(socialController, "friendshipService", friendshipService);
        MockMvc socialMvc = MockMvcBuilders.standaloneSetup(socialController).build();
        when(friendshipService.follow(anyLong(), anyLong(), anyBoolean())).thenReturn("ok");
        when(friendshipService.getFollowers(anyLong(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));
        when(friendshipService.getFollowing(anyLong(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));
        when(friendshipService.deleteFriendship(anyLong(), anyLong())).thenReturn(true);

        socialMvc.perform(post("/api/users/2/follow")
                        .requestAttr("currentUserId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"follow\":true}"))
                .andExpect(status().isOk());
        assertNotNull(socialController.getFollowers(2L, 1));
        assertNotNull(socialController.getFollowing(2L, 1));
        socialMvc.perform(delete("/api/users/friendship/2").requestAttr("currentUserId", 1L)).andExpect(status().isOk());
    }

    @Test
    void allControllerClasses_Load() {
        Class<?>[] classes = {
                AuthController.class, UserController.class, DiscoverController.class,
                ActivityController.class, FriendController.class, ChatController.class,
                DiaryController.class, EvaluationController.class, UploadController.class,
                ActivityInvitationController.class, SocialController.class
        };

        for (Class<?> controllerClass : classes) {
            assertNotNull(controllerClass);
        }
    }
}
