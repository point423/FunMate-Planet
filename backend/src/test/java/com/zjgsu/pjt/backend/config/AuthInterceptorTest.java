package com.zjgsu.pjt.backend.config;

import com.zjgsu.pjt.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthInterceptorTest {

    @Mock
    private JwtUtil jwtUtil;

    private AuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new AuthInterceptor(jwtUtil);
    }

    @Test
    @DisplayName("OPTIONS 预检请求直接放行")
    void preHandle_OPTIONS_PassThrough() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("OPTIONS");
        req.setRequestURI("/api/anything");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(req, resp, new Object());
        assertTrue(result);
        assertEquals(200, resp.getStatus());  // 没改 status
    }

    @Test
    @DisplayName("白名单路径直接放行-login")
    void preHandle_Whitelist_Login() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("POST");
        req.setRequestURI("/api/auth/login");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(req, resp, new Object());
        assertTrue(result);
    }

    @Test
    @DisplayName("白名单路径直接放行-register")
    void preHandle_Whitelist_Register() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("POST");
        req.setRequestURI("/api/auth/register");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(req, resp, new Object());
        assertTrue(result);
    }

    @Test
    @DisplayName("白名单路径直接放行-ai test")
    void preHandle_Whitelist_AiTest() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.setRequestURI("/api/ai/test");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(req, resp, new Object());
        assertTrue(result);
    }

    @Test
    @DisplayName("无 Authorization 头,返回 401")
    void preHandle_NoAuthHeader_401() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.setRequestURI("/api/user/profile");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(req, resp, new Object());
        assertFalse(result);
        assertEquals(401, resp.getStatus());
    }

    @Test
    @DisplayName("Authorization 头不带 Bearer 前缀,返回 401")
    void preHandle_NoBearer_401() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.setRequestURI("/api/user/profile");
        req.addHeader("Authorization", "Basic abc123");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(req, resp, new Object());
        assertFalse(result);
        assertEquals(401, resp.getStatus());
    }

    @Test
    @DisplayName("Token 无效,返回 401")
    void preHandle_InvalidToken_401() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.setRequestURI("/api/user/profile");
        req.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        when(jwtUtil.getUserIdFromToken("invalid-token"))
                .thenThrow(new RuntimeException("Token 无效"));

        boolean result = interceptor.preHandle(req, resp, new Object());
        assertFalse(result);
        assertEquals(401, resp.getStatus());
    }

    @Test
    @DisplayName("Token 有效,通过并将 userId 写入 request")
    void preHandle_ValidToken_Pass() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.setRequestURI("/api/user/profile");
        req.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        when(jwtUtil.getUserIdFromToken("valid-token")).thenReturn(100L);

        boolean result = interceptor.preHandle(req, resp, new Object());
        assertTrue(result);
        // 验证 userId 被写入 request attribute
        assertEquals(100L, req.getAttribute("currentUserId"));
    }
}