package com.hehe.habit_tracker.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Cấp (mở khoá) một thành tựu cho user.
 * habitId = null  → thành tựu account-level.
 * habitId != null → thành tựu gắn với 1 habit.
 */
public record UserAchivementCreationRequest(

        @NotNull(message = "userId is required") Long userId,
        @NotNull(message = "definitionId is required") Long definitionId,
        Long habitId

) {
}
