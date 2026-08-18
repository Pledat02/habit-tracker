package com.hehe.habit_tracker.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hehe.habit_tracker.common.ApiResponse;
import com.hehe.habit_tracker.common.BaseController;
import com.hehe.habit_tracker.dto.response.UserCreationResponse;
import com.hehe.habit_tracker.service.UserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController extends BaseController<UserCreationResponse> {

    UserService userService;

    /**
     * Trả thông tin CHÍNH user đang gọi — xác định qua claim 'sub' của JWT
     * (đã được resource server verify chữ ký), không nhận id từ client.
     * Không public: SecurityConfig đòi Bearer token hợp lệ cho endpoint này.
     */
    @GetMapping("/me")
    public ApiResponse<UserCreationResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return readSuccessResponse(userService.getCurrentUser(jwt.getSubject()));
    }

    @GetMapping("/")
    public ApiResponse<List<UserCreationResponse>> getAll() {
        return readListSuccessResponse(userService.getAllUsers());
    }
}
