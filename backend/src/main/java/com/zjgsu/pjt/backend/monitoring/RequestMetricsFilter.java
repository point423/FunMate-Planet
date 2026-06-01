package com.zjgsu.pjt.backend.monitoring;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class RequestMetricsFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestMetricsFilter.class);

    private final RequestMetrics requestMetrics;

    public RequestMetricsFilter(RequestMetrics requestMetrics) {
        this.requestMetrics = requestMetrics;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.nanoTime();
        Throwable failure = null;
        requestMetrics.incrementActiveRequests();

        try {
            filterChain.doFilter(request, response);
        } catch (ServletException | IOException | RuntimeException ex) {
            failure = ex;
            throw ex;
        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            int status = failure == null ? response.getStatus() : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            requestMetrics.recordRequest(status, durationMs);
            requestMetrics.decrementActiveRequests();

            log.info("event=http_request method={} path={} status={} duration_ms={} error={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    durationMs,
                    failure == null ? "" : failure.getClass().getSimpleName());
        }
    }
}
