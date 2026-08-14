package com.hehe.habit_tracker.common;

/**
 * Loại luật đánh giá của thành tựu — quyết định backend chạy handler nào.
 * Thêm loại mới ở đây, rồi thêm handler tương ứng trong service.
 */
public enum AchievementType {
    STREAK,          // per-habit: 1 habit đạt streak >= target ngày
    TOTAL_CHECKINS,  // account:   tổng số lần check-in >= target
    MULTI_STREAK,    // account:   nhiều habit cùng đạt streak
    HABIT_COUNT,     // account:   duy trì >= target habit
    PERFECT_WEEK,    // account:   hoàn thành 100% trong 1 tuần
    HOLIDAY_CHECKIN; // account:   check-in vào ngày lễ (không cần target)
}
