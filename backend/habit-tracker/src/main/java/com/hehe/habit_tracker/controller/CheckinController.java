package com.hehe.habit_tracker.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hehe.habit_tracker.common.ApiResponse;
import com.hehe.habit_tracker.common.BaseController;
import com.hehe.habit_tracker.dto.request.CheckinCreationRequest;
import com.hehe.habit_tracker.dto.request.CheckinUpdateRequest;
import com.hehe.habit_tracker.dto.response.CheckinResponse;
import com.hehe.habit_tracker.dto.response.CheckinResultResponse;
import com.hehe.habit_tracker.service.CheckinService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/** Chủ sở hữu (qua habit) LUÔN xác định từ claim 'sub' của JWT — không tin habitId/id client gửi mù quáng. */
@RestController
@RequestMapping("/api/v1/checkins")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CheckinController extends BaseController<CheckinResponse> {

    CheckinService checkinService;

    // Trả CheckinResultResponse (khác T=CheckinResponse của base) nên dùng ApiResponse trực tiếp.
    @PostMapping
    public ApiResponse<CheckinResultResponse> create(@Valid @RequestBody CheckinCreationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(checkinService.createCheckin(request, currentUserId(jwt)), 201);
    }

    /**
     * Check-in của mọi habit của chính user đang gọi (Dashboard/Insights).
     * Truyền cả {@code from} và {@code to} (ISO yyyy-MM-dd) để giới hạn khoảng ngày — chặn payload
     * phình vô hạn theo thời gian dùng. Thiếu một trong hai thì trả toàn bộ (tương thích ngược).
     */
    @GetMapping("/me")
    public ApiResponse<List<CheckinResponse>> getMine(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Long userId = currentUserId(jwt);
        List<CheckinResponse> result = (from != null && to != null)
                ? checkinService.getForUserInRange(userId, from, to)
                : checkinService.getAllForUser(userId);
        return readListSuccessResponse(result);
    }

    /** Danh sách check-in của 1 habit. */
    @GetMapping("/habit/{habitId}")
    public ApiResponse<List<CheckinResponse>> getByHabit(@PathVariable Long habitId,
            @AuthenticationPrincipal Jwt jwt) {
        return readListSuccessResponse(checkinService.getCheckinsByHabit(habitId, currentUserId(jwt)));
    }

    @GetMapping("/{id}")
    public ApiResponse<CheckinResponse> getById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return readSuccessResponse(checkinService.getCheckinById(id, currentUserId(jwt)));
    }

    @PutMapping("/{id}")
    public ApiResponse<CheckinResponse> update(@PathVariable Long id,
            @Valid @RequestBody CheckinUpdateRequest request, @AuthenticationPrincipal Jwt jwt) {
        return updateSuccessResponse(checkinService.updateCheckin(id, request, currentUserId(jwt)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<CheckinResponse> delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        checkinService.deleteCheckin(id, currentUserId(jwt));
        return deleteSuccessResponse();
    }
}
