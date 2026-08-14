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
import com.hehe.habit_tracker.dto.request.CheckinCreationRequest;
import com.hehe.habit_tracker.dto.request.CheckinUpdateRequest;
import com.hehe.habit_tracker.dto.response.CheckinResponse;
import com.hehe.habit_tracker.service.CheckinService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/checkins")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CheckinController extends BaseController<CheckinResponse> {

    CheckinService checkinService;

    @PostMapping
    public ApiResponse<CheckinResponse> create(@Valid @RequestBody CheckinCreationRequest request) {
        return createSuccessResponse(checkinService.createCheckin(request));
    }

    /** Danh sách check-in của 1 habit. */
    @GetMapping("/habit/{habitId}")
    public ApiResponse<List<CheckinResponse>> getByHabit(@PathVariable Long habitId) {
        return ApiResponse.success(checkinService.getCheckinsByHabit(habitId), 200);
    }

    @GetMapping("/{id}")
    public ApiResponse<CheckinResponse> getById(@PathVariable Long id) {
        return readSuccessResponse(checkinService.getCheckinById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<CheckinResponse> update(@PathVariable Long id,
            @Valid @RequestBody CheckinUpdateRequest request) {
        return updateSuccessResponse(checkinService.updateCheckin(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<CheckinResponse> delete(@PathVariable Long id) {
        checkinService.deleteCheckin(id);
        return deleteSuccessResponse();
    }
}
