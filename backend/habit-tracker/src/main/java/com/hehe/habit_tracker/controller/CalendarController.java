package com.hehe.habit_tracker.controller;

import java.net.URI;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hehe.habit_tracker.common.ApiResponse;
import com.hehe.habit_tracker.common.BaseController;
import com.hehe.habit_tracker.service.GoogleCalendarService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/** Đồng bộ 1 chiều habit -> Google Calendar. Bật/tắt bằng flag app.google-calendar.enabled. */
@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CalendarController extends BaseController<Void> {

    GoogleCalendarService calendarService;

    @org.springframework.beans.factory.annotation.Value("${app.frontend-origin}")
    @lombok.experimental.NonFinal
    String frontendOrigin;

    /** Trạng thái: tính năng có bật không + user đã kết nối chưa. */
    @GetMapping("/status")
    public ApiResponse<Map<String, Boolean>> status(@AuthenticationPrincipal Jwt jwt) {
        Long userId = currentUserId(jwt);
        return ApiResponse.success(Map.of(
                "enabled", calendarService.isEnabled(),
                "connected", calendarService.isEnabled() && calendarService.isConnected(userId)), 200);
    }

    /** Trả URL để frontend chuyển hướng user sang Google xin quyền Calendar. */
    @GetMapping("/authorize")
    public ApiResponse<Map<String, String>> authorize(@AuthenticationPrincipal Jwt jwt) {
        String url = calendarService.buildAuthorizeUrl(currentUserId(jwt));
        return ApiResponse.success(Map.of("url", url), 200);
    }

    /**
     * Google redirect về đây (public, không có Bearer) — nhận diện user qua state đã ký.
     * Params optional: Google có thể trả {@code ?error=access_denied} (user từ chối) thay vì code.
     * Mọi trường hợp đều REDIRECT về frontend (không trả 4xx/5xx thô cho trình duyệt người dùng).
     */
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error) {
        String outcome = "connected";
        if (error != null || code == null || state == null) {
            outcome = "error";
        } else {
            try {
                calendarService.handleCallback(code, state);
            } catch (Exception e) {
                outcome = "error";
            }
        }
        return ResponseEntity.status(302)
                .location(URI.create(frontendOrigin + "/profile?calendar=" + outcome)).build();
    }

    /** Ngắt kết nối Calendar của user hiện tại. */
    @PostMapping("/disconnect")
    public ApiResponse<Void> disconnect(@AuthenticationPrincipal Jwt jwt) {
        calendarService.disconnect(currentUserId(jwt));
        return ApiResponse.success(null, 200);
    }

    /** Đẩy toàn bộ habit hiện có của user lên Calendar (tạo/cập nhật event). */
    @PostMapping("/sync")
    public ApiResponse<Void> sync(@AuthenticationPrincipal Jwt jwt) {
        calendarService.syncAllForUser(currentUserId(jwt));
        return ApiResponse.success(null, 200);
    }
}
