package com.hehe.habit_tracker.dto.request;

/** Hiện chỉ cho phép đổi trạng thái đã chia sẻ. */
public record UserAchivementUpdateRequest(
        Boolean shared
) {
}
