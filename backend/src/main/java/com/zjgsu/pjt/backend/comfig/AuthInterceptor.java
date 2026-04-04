package com.zjgsu.pjt.backend.comfig;

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

    private static final Set<String> WHITE_LIST = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/test/connection/db",
            "/api/test/connection/redis",
            "/api/test/connection/all"
    );

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // 放行白名单
        for (String path : WHITE_LIST) {
            if (uri.startsWith(path)) return true;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            write401(response, "未登录或缺少Token");
            return false;
        }

        String token = auth.substring(7);
        try {
            // ✅ 修正方法名为 getUserIdFromToken
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
