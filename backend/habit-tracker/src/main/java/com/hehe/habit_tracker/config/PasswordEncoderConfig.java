package com.hehe.habit_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Tách riêng khỏi SecurityConfig để tránh circular dependency:
 * SecurityConfig -> OAuth2LoginSuccessHandler -> AuthenticationService -> PasswordEncoder.
 * Nếu bean này định nghĩa bên trong SecurityConfig, Spring phải dựng xong
 * OAuth2LoginSuccessHandler (constructor injection) TRƯỚC KHI SecurityConfig
 * sẵn sàng cung cấp PasswordEncoder cho chính chuỗi phụ thuộc đó -> vòng lặp
 * không gỡ được, app crash ngay lúc khởi động (không phát hiện được lúc compile).
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
