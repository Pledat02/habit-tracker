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
import com.hehe.habit_tracker.dto.request.HabitCreationRequest;
import com.hehe.habit_tracker.dto.request.HabitUpdateRequest;
import com.hehe.habit_tracker.dto.response.HabitResponse;
import com.hehe.habit_tracker.service.HabitService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Habit là dữ liệu riêng của từng user. Chủ sở hữu LUÔN xác định qua claim 'sub'
 * của JWT đã verify (@AuthenticationPrincipal Jwt) — không bao giờ nhận userId
 * từ query/body, tránh user A thao túng dữ liệu của user B.
 */
@RestController
@RequestMapping("/api/v1/habits")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HabitController extends BaseController<HabitResponse> {

    HabitService habitService;

    @PostMapping
    public ApiResponse<HabitResponse> create(@Valid @RequestBody HabitCreationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return createSuccessResponse(habitService.createHabit(request, currentUserId(jwt)));
    }

    @GetMapping
    public ApiResponse<List<HabitResponse>> getAll(@AuthenticationPrincipal Jwt jwt) {
        return readListSuccessResponse(habitService.getAllHabits(currentUserId(jwt)));
    }

    @GetMapping("/{id}")
    public ApiResponse<HabitResponse> getById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return readSuccessResponse(habitService.getHabitById(id, currentUserId(jwt)));
    }

    @PutMapping("/{id}")
    public ApiResponse<HabitResponse> update(@PathVariable Long id, @Valid @RequestBody HabitUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return updateSuccessResponse(habitService.updateHabit(id, request, currentUserId(jwt)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<HabitResponse> delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        habitService.deleteHabit(id, currentUserId(jwt));
        return deleteSuccessResponse();
    }
}
