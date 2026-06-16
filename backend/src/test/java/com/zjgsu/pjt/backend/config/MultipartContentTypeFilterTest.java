package com.zjgsu.pjt.backend.config;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MultipartContentTypeFilterTest {

    private final MultipartContentTypeFilter filter = new MultipartContentTypeFilter();

    @Test
    void doFilterInternal_RemovesCharsetFromMultipartContentType() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("multipart/form-data; boundary=abc; charset=UTF-8");
        request.addHeader("Content-Type", "multipart/form-data; boundary=abc; charset=UTF-8");
        MockHttpServletResponse response = new MockHttpServletResponse();
        CapturingFilterChain chain = new CapturingFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.capturedRequest.getContentType()).isEqualTo("multipart/form-data; boundary=abc");
        assertThat(chain.capturedRequest.getHeader("Content-Type")).isEqualTo("multipart/form-data; boundary=abc");
        assertThat(Collections.list(chain.capturedRequest.getHeaders("Content-Type")))
                .containsExactly("multipart/form-data; boundary=abc");
    }

    @Test
    void doFilterInternal_LeavesNonMultipartContentTypeUnchanged() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json;charset=UTF-8");
        MockHttpServletResponse response = new MockHttpServletResponse();
        CapturingFilterChain chain = new CapturingFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.capturedRequest).isSameAs(request);
        assertThat(chain.capturedRequest.getContentType()).isEqualTo("application/json;charset=UTF-8");
    }

    private static class CapturingFilterChain implements jakarta.servlet.FilterChain {
        private HttpServletRequest capturedRequest;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            this.capturedRequest = (HttpServletRequest) request;
        }
    }
}
