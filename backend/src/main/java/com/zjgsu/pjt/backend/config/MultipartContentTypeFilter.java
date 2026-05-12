package com.zjgsu.pjt.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter to sanitize Content-Type header for multipart requests.
 * Some clients accidentally append a charset parameter after the boundary
 * (e.g. "multipart/form-data;boundary=...;charset=UTF-8"), which Tomcat/Spring
 * may reject. This filter removes any ";charset=..." suffix when Content-Type
 * contains multipart/form-data.
 */
@Component
public class MultipartContentTypeFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ct = request.getContentType();
        if (ct != null && ct.toLowerCase().contains("multipart/form-data") && ct.toLowerCase().contains("charset=")) {
            String fixed = stripCharset(ct);
            HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request) {
                @Override
                public String getContentType() {
                    return fixed;
                }

                @Override
                public String getHeader(String name) {
                    if ("content-type".equalsIgnoreCase(name)) return fixed;
                    return super.getHeader(name);
                }
            };
            filterChain.doFilter(wrapper, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String stripCharset(String ct) {
        // remove any ;charset=... token but keep boundary
        // e.g. "multipart/form-data;boundary=----WebKit...;charset=UTF-8" -> "multipart/form-data;boundary=----WebKit..."
        int idx = ct.toLowerCase().indexOf(";charset=");
        if (idx > -1) {
            return ct.substring(0, idx);
        }
        return ct;
    }
}
