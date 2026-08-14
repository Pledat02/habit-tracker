package com.hehe.habit_tracker.controller;

import java.util.List;

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

/** Mở khoá (cấp) thành tựu cho user + đọc danh sách đã mở khoá. */
@RestController
@RequestMapping("/api/v1/user-achievements")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserAchivementController extends BaseController<UserAchivementResponse> {

    UserAchivementService userAchivementService;

    /** Cấp một thành tựu cho user. */
    @PostMapping
    public ApiResponse<UserAchivementResponse> grant(@Valid @RequestBody UserAchivementCreationRequest request) {
        return createSuccessResponse(userAchivementService.grant(request));
    }

    /** Tất cả thành tựu đã mở khoá của 1 user. */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<UserAchivementResponse>> getByUser(@PathVariable Long userId) {
        return ApiResponse.success(userAchivementService.getByUser(userId), 200);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserAchivementResponse> getById(@PathVariable Long id) {
        return readSuccessResponse(userAchivementService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserAchivementResponse> update(@PathVariable Long id,
            @Valid @RequestBody UserAchivementUpdateRequest request) {
        return updateSuccessResponse(userAchivementService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<UserAchivementResponse> delete(@PathVariable Long id) {
        userAchivementService.delete(id);
        return deleteSuccessResponse();
    }
}
