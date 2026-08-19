package com.hehe.habit_tracker.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Value("${app.frontend-origin}")
    private String frontendOrigin;

    /** Endpoint công khai (không cần token). */
    private static final String[] PUBLIC_POST = {
            "/auth/login",
            "/auth/register",
            // Không đòi Bearer token: access token cũ có thể đã hết hạn, bảo mật
            // của 2 endpoint này nằm ở cookie HttpOnly (refresh token), không phải header.
            "/auth/refresh",
            "/auth/logout",
            "/auth/password/forgot", // xin link reset (public: user chưa đăng nhập được)
            "/auth/password/reset", // đặt lại mật khẩu bằng token trong email
            "/auth/email/verify", // xác thực email bằng token (public: bấm từ link email)
            "/auth/email/resend" // gửi lại email xác thực
    };
    private static final String[] PUBLIC_GET = {
            "/api/v1/achievements/**", // catalog thành tựu: ai xem cũng được
            "/oauth2/jwks", // public key để verify token
            "/oauth2/authorization/**", // bước 1: bắt đầu redirect sang Google
            "/login/oauth2/code/**", // bước 2: Google redirect callback về đây
            "/actuator/health", // health cho load balancer/uptime: công khai, không lộ chi tiết
            "/actuator/health/**",
            "/api/v1/calendar/callback" // Google redirect về (không có Bearer) — xác thực qua state đã ký
            // metrics/khác dưới /actuator vẫn đòi token (rơi vào anyRequest().authenticated()).
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // BẮT BUỘC cho frontend :5173 gọi backend :8080 (cross-origin) kèm cookie
        // refresh_token (credentials:'include'). Thiếu dòng này, trình duyệt tự
        // chặn response trước khi JS kịp đọc, kể cả khi backend trả 200 hợp lệ.
        http.cors(Customizer.withDefaults());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, PUBLIC_POST).permitAll()
                .requestMatchers(HttpMethod.GET, PUBLIC_GET).permitAll()
                .anyRequest().authenticated());

        // Resource server: tự động dùng bean JwtDecoder (RSA public key) ở KeyConfig
        // -> validate JWT của CHÍNH app khi gọi các API còn lại.
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        // Client OAuth2: backend tự redirect sang Google, tự đổi code lấy thông tin
        // user.
        // successHandler thay Users Google -> phát JWT riêng của app -> redirect về
        // frontend.
        http.oauth2Login(oauth2 -> oauth2.successHandler(oAuth2LoginSuccessHandler));

        // IF_REQUIRED (không phải STATELESS): oauth2Login cần 1 session NGẮN HẠN để lưu
        // state + PKCE code_verifier giữa bước redirect sang Google và bước Google gọi
        // callback về. STATELESS sẽ làm hỏng bước so khớp đó. Các API còn lại xác thực
        // bằng Bearer JWT vẫn không tạo/dùng session trong thực tế.
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    // PasswordEncoder chuyển sang PasswordEncoderConfig — xem file đó để biết lý do
    // (circular dependency: SecurityConfig -> OAuth2LoginSuccessHandler ->
    // AuthenticationService -> PasswordEncoder nếu để chung 1 class).

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // KHÔNG dùng "*": trình duyệt chặn allowCredentials=true đi kèm wildcard origin.
        config.setAllowedOrigins(List.of(frontendOrigin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // để trình duyệt gửi/nhận cookie refresh_token

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
