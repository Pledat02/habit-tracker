package com.hehe.habit_tracker.controller;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hehe.habit_tracker.common.ApiResponse;
import com.hehe.habit_tracker.common.BaseController;
import com.hehe.habit_tracker.config.RefreshCookieUtil;
import com.hehe.habit_tracker.dto.request.AuthenticationRequest;
import com.hehe.habit_tracker.dto.request.UserCreationRequest;
import com.hehe.habit_tracker.dto.response.AuthenticationResponse;
import com.hehe.habit_tracker.dto.response.UserCreationResponse;
import com.hehe.habit_tracker.service.AuthenticationService;
import com.hehe.habit_tracker.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/** Endpoint công khai: đăng ký, đăng nhập, làm mới access token, đăng xuất. */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController extends BaseController<AuthenticationResponse> {

    AuthenticationService authenticationService;
    UserService userService;

    // Kiểu trả về (UserCreationResponse) khác T của base nên dùng ApiResponse trực tiếp,
    // giống cách các endpoint trả List ở những controller khác.
    @PostMapping("/register")
    public ApiResponse<UserCreationResponse> register(@Valid @RequestBody UserCreationRequest request) {
        return ApiResponse.success(userService.createUser(request), 201);
    }

    // AuthenticationResponse == T của base → dùng helper (200).
    // response: nơi service đặt cookie refresh token (HttpOnly) song song với body trả về.
    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request,
            HttpServletResponse response) {
        return readSuccessResponse(authenticationService.authenticate(request, response));
    }

    /**
     * Đổi refresh token (đọc từ cookie HttpOnly) lấy access token mới.
     * KHÔNG yêu cầu Authorization header — access token cũ có thể đã hết hạn,
     * chính vì vậy endpoint này phải public (xem SecurityConfig).
     */
    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(
            @CookieValue(name = RefreshCookieUtil.COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response) {
        return readSuccessResponse(authenticationService.refresh(refreshToken, response));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(name = RefreshCookieUtil.COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response) {
        authenticationService.logout(refreshToken, response);
        return ApiResponse.success(null, 200);
    }
}
