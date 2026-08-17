package com.hehe.habit_tracker.dto.request;

import com.hehe.habit_tracker.common.AchievementCategory;
import com.hehe.habit_tracker.common.AchievementType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AchivementCreationRequest(

        @NotBlank(message = "code is required") String code,
        @NotNull(message = "category is required") AchievementCategory category,
        @NotNull(message = "type is required") AchievementType type,
        @NotBlank(message = "name is required") String name,
        String description,
        String icon,
        /** Ngưỡng chính (STREAK: ngày, MULTI_STREAK: số habit). */
        Integer target,
        /** Ngưỡng phụ (MULTI_STREAK: số ngày streak tối thiểu mỗi habit). */
        Integer target2,
        // sortOrder KHÔNG nhận từ client: service tự đặt = max + 10 khi tạo.
        /** NULL => mặc định true (xử lý ở service/entity). */
        Boolean active

) {
}
