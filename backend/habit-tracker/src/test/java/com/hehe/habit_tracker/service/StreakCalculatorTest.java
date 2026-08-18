package com.hehe.habit_tracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.hehe.habit_tracker.entity.Habit;

/**
 * Unit test thuần (không Spring, không Docker) cho logic tính streak của engine.
 * Dùng overload currentStreak(habit, dates, TODAY) với TODAY CỐ ĐỊNH -> kết quả
 * không phụ thuộc ngày chạy (trước đây test bị flaky, fail khi CI chạy vào thứ Ba).
 */
class StreakCalculatorTest {

    private final StreakCalculator calc = new StreakCalculator();

    // 2024-01-03 là THỨ TƯ (mốc cố định cho mọi test bên dưới).
    private static final LocalDate TODAY = LocalDate.of(2024, 1, 3);

    private Habit habitWithFrequency(String frequency) {
        Habit h = new Habit();
        h.setFrequency(frequency);
        return h;
    }

    @Test
    void dailyHabit_consecutiveDaysEndingToday_countsAll() {
        Habit habit = habitWithFrequency("\"daily\"");
        List<LocalDate> checkins = List.of(TODAY, TODAY.minusDays(1), TODAY.minusDays(2));

        assertEquals(3, calc.currentStreak(habit, checkins, TODAY));
    }

    @Test
    void dailyHabit_todayNotDoneYet_doesNotBreakStreak() {
        Habit habit = habitWithFrequency("\"daily\"");
        // Hôm nay chưa check-in, nhưng 2 hôm trước liên tục -> streak vẫn = 2.
        List<LocalDate> checkins = List.of(TODAY.minusDays(1), TODAY.minusDays(2));

        assertEquals(2, calc.currentStreak(habit, checkins, TODAY));
    }

    @Test
    void dailyHabit_gapInPast_breaksStreak() {
        Habit habit = habitWithFrequency("\"daily\"");
        // Thiếu ngày (today-1) -> chỉ tính được đúng hôm nay.
        List<LocalDate> checkins = List.of(TODAY, TODAY.minusDays(2), TODAY.minusDays(3));

        assertEquals(1, calc.currentStreak(habit, checkins, TODAY));
    }

    @Test
    void noCheckins_streakZero() {
        Habit habit = habitWithFrequency("\"daily\"");
        assertEquals(0, calc.currentStreak(habit, List.of(), TODAY));
    }

    @Test
    void specificDays_onlyCountsScheduledWeekdays() {
        // Chỉ lên lịch Thứ 2 (1) và Thứ 4 (3) — quy ước JS 0=CN..6=T7.
        Habit habit = habitWithFrequency("{\"type\":\"days\",\"days\":[1,3]}");

        // TODAY = Thứ Tư 2024-01-03; Thứ Hai cùng tuần = 2024-01-01.
        LocalDate wed = TODAY;               // scheduled + có check-in
        LocalDate mon = TODAY.minusDays(2);  // scheduled + có check-in (Thứ Ba ở giữa không có lịch, bỏ qua)

        assertEquals(2, calc.currentStreak(habit, List.of(wed, mon), TODAY));
    }
}
