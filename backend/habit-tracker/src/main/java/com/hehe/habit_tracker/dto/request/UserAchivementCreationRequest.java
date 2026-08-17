package com.hehe.habit_tracker.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Cấp (mở khoá) một thành tựu cho CHÍNH user đang gọi — userId KHÔNG nằm trong
 * request, luôn suy ra từ JWT (xem UserAchivementController). Trước đây nhận
 * thẳng userId từ client, nghĩa là ai cũng cấp được thành tựu cho bất kỳ ai.
 * habitId = null  → thành tựu account-level.
 * habitId != null → thành tựu gắn với 1 habit (phải là habit của chính user này).
 */
public record UserAchivementCreationRequest(

        @NotNull(message = "definitionId is required") Long definitionId,
        Long habitId

) {
}
