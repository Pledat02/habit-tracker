package com.hehe.habit_tracker.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hehe.habit_tracker.common.ApiResponse;
import com.hehe.habit_tracker.common.BaseController;
import com.hehe.habit_tracker.dto.request.UserAchivementCreationRequest;
import com.hehe.habit_tracker.dto.request.UserAchivementUpdateRequest;
import com.hehe.habit_tracker.dto.response.UserAchivementResponse;
import com.hehe.habit_tracker.service.UserAchivementService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Mở khoá (cấp) thành tựu cho CHÍNH user đang gọi + đọc danh sách đã mở khoá.
 * Chủ sở hữu LUÔN xác định từ claim 'sub' của JWT — không nhận userId từ client.
 */
@RestController
@RequestMapping("/api/v1/user-achievements")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserAchivementController extends BaseController<UserAchivementResponse> {

    UserAchivementService userAchivementService;

    /** Cấp một thành tựu cho chính user đang gọi. */
    @PostMapping
    public ApiResponse<UserAchivementResponse> grant(@Valid @RequestBody UserAchivementCreationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return createSuccessResponse(userAchivementService.grant(request, currentUserId(jwt)));
    }

    /** Tất cả thành tựu đã mở khoá của chính user đang gọi. */
    @GetMapping("/me")
    public ApiResponse<List<UserAchivementResponse>> getMine(@AuthenticationPrincipal Jwt jwt) {
        return readListSuccessResponse(userAchivementService.getMine(currentUserId(jwt)));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserAchivementResponse> getById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return readSuccessResponse(userAchivementService.getById(id, currentUserId(jwt)));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserAchivementResponse> update(@PathVariable Long id,
            @Valid @RequestBody UserAchivementUpdateRequest request, @AuthenticationPrincipal Jwt jwt) {
        return updateSuccessResponse(userAchivementService.update(id, request, currentUserId(jwt)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<UserAchivementResponse> delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        userAchivementService.delete(id, currentUserId(jwt));
        return deleteSuccessResponse();
    }
}
