package com.zjgsu.pjt.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

/**
 * 终极修复：彻底剥离 multipart 请求中非法的 charset 参数
 * 专门解决 "Content-Type 'multipart/form-data;...;charset=UTF-8' is not supported"
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 确保在 Spring 所有解析器之前执行
public class MultipartContentTypeFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String contentType = request.getContentType();
        // 如果是 multipart 请求且带着非法 charset 后缀
        if (contentType != null && contentType.toLowerCase().contains("multipart/form-data") 
                && contentType.toLowerCase().contains("charset=")) {
            
            // 暴力剪掉 ;charset=UTF-8
            final String fixedContentType = contentType.replaceAll("(?i);\\s*charset=[^;]*", "");
            
            HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request) {
                @Override
                public String getContentType() {
                    return fixedContentType;
                }

                @Override
                public String getHeader(String name) {
                    if ("Content-Type".equalsIgnoreCase(name)) return fixedContentType;
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("Content-Type".equalsIgnoreCase(name)) {
                        return Collections.enumeration(Collections.singletonList(fixedContentType));
                    }
                    return super.getHeaders(name);
                }
            };
            filterChain.doFilter(wrapper, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
