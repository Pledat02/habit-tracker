package com.hehe.habit_tracker.dto.response;

import java.time.Instant;
import java.time.LocalTime;

import com.hehe.habit_tracker.entity.IconHabit;

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
        IconHabit iconHabit,
        Instant createdAt,
        Instant updatedAt
) {
}
