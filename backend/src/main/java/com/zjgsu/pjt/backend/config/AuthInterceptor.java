package com.zjgsu.pjt.backend.config;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 关键修复：将 AI 测试接口加入拦截器自身的白名单
    private static final Set<String> WHITE_LIST = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/test/connection",
            "/api/upload/static",
            "/api/ai/test"
    );

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        
        // 白名单放行逻辑
        for (String path : WHITE_LIST) {
            if (uri.startsWith(path)) return true;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            write401(response, "未登录或令牌缺失");
            return false;
        }

        String token = auth.substring(7);
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            request.setAttribute("currentUserId", userId);
            return true;
        } catch (Exception e) {
            write401(response, "Token无效或已过期");
            return false;
        }
    }

    private void write401(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        Result<Object> r = Result.error(401, msg);
        response.getWriter().write(objectMapper.writeValueAsString(r));
    }
}
