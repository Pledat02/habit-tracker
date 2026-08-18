package com.hehe.habit_tracker.dto.response;

import java.time.Instant;

import com.hehe.habit_tracker.common.AchievementCategory;
import com.hehe.habit_tracker.common.AchievementType;

import lombok.Builder;

/**
 * Phẳng hoá: gộp thông tin từ definition (code/name/icon/type/category)
 * để frontend hiển thị mà không cần gọi thêm.
 */
@Builder
public record UserAchivementResponse(
        Long id,
        Long userId,
        Long definitionId,
        String code,
        String name,
        String icon,
        AchievementType type,
        AchievementCategory category,
        Long habitId,
        Instant unlockedAt,
        boolean shared
) {
}
