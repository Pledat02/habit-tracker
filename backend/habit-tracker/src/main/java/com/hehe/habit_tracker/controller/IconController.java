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
import com.hehe.habit_tracker.dto.request.IconCreationRequest;
import com.hehe.habit_tracker.dto.request.IconUpdateRequest;
import com.hehe.habit_tracker.dto.response.IconResponse;
import com.hehe.habit_tracker.service.IconService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/icons")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IconController extends BaseController<IconResponse> {

    IconService iconService;

    @PostMapping
    public ApiResponse<IconResponse> create(@Valid @RequestBody IconCreationRequest request) {
        return createSuccessResponse(iconService.createIcon(request));
    }

    @GetMapping
    public ApiResponse<List<IconResponse>> getAll() {
        return readListSuccessResponse(iconService.getAllIcons());
    }

    @GetMapping("/{id}")
    public ApiResponse<IconResponse> getById(@PathVariable Long id) {
        return readSuccessResponse(iconService.getIconById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<IconResponse> update(@PathVariable Long id, @Valid @RequestBody IconUpdateRequest request) {
        return updateSuccessResponse(iconService.updateIcon(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<IconResponse> delete(@PathVariable Long id) {
        iconService.deleteIcon(id);
        return deleteSuccessResponse();
    }
}
