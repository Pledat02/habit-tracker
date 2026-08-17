package com.hehe.habit_tracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import lombok.experimental.NonFinal;

/**
 * Dựng cookie chứa refresh token — dùng chung cho login thường và login Google
 * (cả hai đều cần đặt/xoá cùng 1 loại cookie theo cùng 1 cấu hình).
 *
 * httpOnly(true): JS KHÔNG đọc được cookie này (kể cả nếu trang dính XSS) — đây là
 * lý do chọn cookie thay vì localStorage cho refresh token.
 * path("/auth"): chỉ gửi kèm cookie khi gọi đúng các endpoint /auth/* (refresh, logout),
 * không gửi kèm mọi request khác — giảm diện bị lộ.
 */
@Component
public class RefreshCookieUtil {

    public static final String COOKIE_NAME = "refresh_token";

    @Value("${app.cookie-secure}")
    @NonFinal
    boolean secure;

    @Value("${jwt.refreshValidDuration}")
    @NonFinal
    long maxAgeSeconds;

    public String build(String rawToken) {
        return ResponseCookie.from(COOKIE_NAME, rawToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/auth")
                .maxAge(maxAgeSeconds)
                .build()
                .toString();
    }

    /** Cookie rỗng, hết hạn ngay -> trình duyệt tự xoá. Dùng lúc logout. */
    public String clear() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/auth")
                .maxAge(0)
                .build()
                .toString();
    }
}
