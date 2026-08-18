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
import com.hehe.habit_tracker.dto.request.AchivementCreationRequest;
import com.hehe.habit_tracker.dto.request.AchivementUpdateRequest;
import com.hehe.habit_tracker.dto.response.AchivementResponse;
import com.hehe.habit_tracker.service.AchivementService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/** CRUD catalog định nghĩa thành tựu (admin quản lý). */
@RestController
@RequestMapping("/api/v1/achievements")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AchivementController extends BaseController<AchivementResponse> {

    AchivementService achivementService;

    @PostMapping
    public ApiResponse<AchivementResponse> create(@Valid @RequestBody AchivementCreationRequest request) {
        return createSuccessResponse(achivementService.createAchivement(request));
    }

    @GetMapping
    public ApiResponse<List<AchivementResponse>> getAll() {
        return readListSuccessResponse(achivementService.getAllAchivements());
    }

    @GetMapping("/{id}")
    public ApiResponse<AchivementResponse> getById(@PathVariable Long id) {
        return readSuccessResponse(achivementService.getAchivementById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<AchivementResponse> update(@PathVariable Long id,
            @Valid @RequestBody AchivementUpdateRequest request) {
        return updateSuccessResponse(achivementService.updateAchivement(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<AchivementResponse> delete(@PathVariable Long id) {
        achivementService.deleteAchivement(id);
        return deleteSuccessResponse();
    }
}
