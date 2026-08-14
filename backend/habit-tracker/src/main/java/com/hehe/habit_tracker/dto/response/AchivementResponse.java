package com.hehe.habit_tracker.dto.response;

import java.time.Instant;

import com.hehe.habit_tracker.common.AchievementCategory;
import com.hehe.habit_tracker.common.AchievementType;

import lombok.Builder;

@Builder
public record AchivementResponse(
        Long id,
        String code,
        AchievementCategory category,
        AchievementType type,
        String name,
        String description,
        String icon,
        Integer target,
        int sortOrder,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
