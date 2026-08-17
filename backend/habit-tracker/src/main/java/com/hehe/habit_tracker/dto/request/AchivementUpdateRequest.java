package com.hehe.habit_tracker.dto.request;

import com.hehe.habit_tracker.common.AchievementCategory;
import com.hehe.habit_tracker.common.AchievementType;

/**
 * Cập nhật một phần — mọi field null sẽ bị bỏ qua (không đổi).
 * Không cho đổi `code` vì đó là danh tính bất biến của thành tựu.
 */
public record AchivementUpdateRequest(
        AchievementCategory category,
        AchievementType type,
        String name,
        String description,
        String icon,
        Integer target,
        Integer target2,
        Integer sortOrder,
        Boolean active
) {
}
