package com.hehe.habit_tracker.dto.response;

import java.time.Instant;
import java.time.LocalTime;

import lombok.Builder;

@Builder
public record HabitResponse(
        Long id,
        String name,
        String frequency,
        String note,
        LocalTime remindTime,
        boolean isPaused,
        int bestStreak,
        String icon,
        String iconColor,
        Instant createdAt,
        Instant updatedAt
) {
}
