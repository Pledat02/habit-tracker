package com.hehe.habit_tracker.dto.response;

import java.util.List;

import lombok.Builder;

/**
 * Kết quả của 1 lần tạo check-in: bản ghi check-in + các thành tựu VỪA MỚI mở khoá
 * do check-in này kích hoạt (engine đánh giá đồng bộ). Frontend đọc newAchievements
 * để hiện chúc mừng, thay vì tự tính/cấp thủ công như trước.
 */
@Builder
public record CheckinResultResponse(
        CheckinResponse checkin,
        List<UserAchivementResponse> newAchievements
) {
}
