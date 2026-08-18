package com.hehe.habit_tracker.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.hehe.habit_tracker.service.RateLimiter;

/**
 * Đăng ký RateLimitFilter chỉ cho các URL auth nhạy cảm. Đặt trước Spring Security filter chain
 * (HIGHEST_PRECEDENCE) để chặn sớm, khỏi tốn công xử lý request đã vượt ngưỡng.
 */
@Configuration
public class RateLimitConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimiter rateLimiter) {
        FilterRegistrationBean<RateLimitFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new RateLimitFilter(rateLimiter));
        reg.addUrlPatterns("/auth/login", "/auth/refresh", "/auth/register");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return reg;
    }
}
