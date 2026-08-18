package com.hehe.habit_tracker.config;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hehe.habit_tracker.exception.ErrorCode;
import com.hehe.habit_tracker.service.RateLimiter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Chặn brute-force các endpoint auth nhạy cảm: giới hạn số request theo IP (token bucket).
 * Chỉ map vào /auth/login, /auth/refresh, /auth/register (xem RateLimitConfig) — KHÔNG chặn
 * logout để user luôn thoát được. Vượt ngưỡng -> 429 với body ApiResponse đồng nhất.
 */
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiter rateLimiter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String key = clientIp(request) + ":" + request.getRequestURI();
        if (rateLimiter.tryAcquire(key)) {
            chain.doFilter(request, response);
            return;
        }
        ErrorCode ec = ErrorCode.RATE_LIMITED;
        response.setStatus(ec.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        // Viết tay JSON khớp record ApiResponse(data, message, status) — jackson-databind không
        // có trên compile classpath (chỉ runtime), nên không import ObjectMapper ở đây.
        String body = "{\"data\":null,\"message\":\"" + escape(ec.getMessage()) + "\",\"status\":" + ec.getCode() + "}";
        response.getWriter().write(body);
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** IP client: ưu tiên hop đầu của X-Forwarded-For (khi sau proxy), fallback remoteAddr. */
    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
