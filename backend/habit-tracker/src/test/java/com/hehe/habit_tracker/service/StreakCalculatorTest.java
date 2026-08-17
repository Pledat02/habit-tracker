package com.hehe.habit_tracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.hehe.habit_tracker.entity.Habit;

/** Unit test thuần (không Spring, không Docker) cho logic tính streak của engine. */
class StreakCalculatorTest {

    private final StreakCalculator calc = new StreakCalculator();

    private Habit habitWithFrequency(String frequency) {
        Habit h = new Habit();
        h.setFrequency(frequency);
        return h;
    }

    @Test
    void dailyHabit_consecutiveDaysEndingToday_countsAll() {
        Habit habit = habitWithFrequency("\"daily\"");
        LocalDate today = LocalDate.now();
        List<LocalDate> checkins = List.of(today, today.minusDays(1), today.minusDays(2));

        assertEquals(3, calc.currentStreak(habit, checkins));
    }

    @Test
    void dailyHabit_todayNotDoneYet_doesNotBreakStreak() {
        Habit habit = habitWithFrequency("\"daily\"");
        LocalDate today = LocalDate.now();
        // Hôm nay chưa check-in, nhưng 2 hôm trước liên tục -> streak vẫn = 2.
        List<LocalDate> checkins = List.of(today.minusDays(1), today.minusDays(2));

        assertEquals(2, calc.currentStreak(habit, checkins));
    }

    @Test
    void dailyHabit_gapInPast_breaksStreak() {
        Habit habit = habitWithFrequency("\"daily\"");
        LocalDate today = LocalDate.now();
        // Thiếu ngày (today-1) -> chỉ tính được đúng hôm nay.
        List<LocalDate> checkins = List.of(today, today.minusDays(2), today.minusDays(3));

        assertEquals(1, calc.currentStreak(habit, checkins));
    }

    @Test
    void noCheckins_streakZero() {
        Habit habit = habitWithFrequency("\"daily\"");
        assertEquals(0, calc.currentStreak(habit, List.of()));
    }

    @Test
    void specificDays_onlyCountsScheduledWeekdays() {
        // Chỉ lên lịch Thứ 2 (1) và Thứ 4 (3). Ngày không có lịch bị bỏ qua, không làm đứt streak.
        Habit habit = habitWithFrequency("{\"type\":\"days\",\"days\":[1,3]}");

        // Tìm Thứ 4 gần nhất <= hôm nay để test ổn định bất kể hôm nay là thứ mấy.
        LocalDate wed = LocalDate.now();
        while (wed.getDayOfWeek().getValue() != 3) {
            wed = wed.minusDays(1);
        }
        LocalDate mon = wed.minusDays(2); // Thứ 2 cùng tuần

        assertEquals(2, calc.currentStreak(habit, List.of(wed, mon)));
    }
}
