package com.hehe.habit_tracker.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.hehe.habit_tracker.entity.Habit;

/**
 * Tính streak (số ngày liên tục) cho 1 habit — bản backend của currentStreak ở
 * frontend (utils.ts). Đếm ngược từ hôm nay: mỗi NGÀY-CÓ-LỊCH đã check-in thì +1,
 * gặp ngày-có-lịch bỏ lỡ (không phải hôm nay) thì dừng. Ngày không có lịch bỏ qua.
 *
 * frequency lưu dạng JSON: "daily"/"weekly_3"/"weekly_5" (coi như có lịch mọi ngày,
 * khớp hành vi frontend hiện tại) hoặc {"type":"days","days":[0..6]} (0=CN..6=T7).
 * Parse thủ công (không dùng Jackson) vì tập giá trị nhỏ và cố định.
 */
@Component
public class StreakCalculator {

    private static final int MAX_LOOKBACK_DAYS = 730;
    // Bắt mảng số trong "days":[...] của frequency dạng {"type":"days","days":[1,3,5]}.
    private static final Pattern DAYS_ARRAY = Pattern.compile("\"days\"\\s*:\\s*\\[([0-9,\\s]*)]");

    public int currentStreak(Habit habit, List<LocalDate> checkinDates) {
        Set<LocalDate> done = new HashSet<>(checkinDates);
        Set<Integer> scheduledDays = parseScheduledDays(habit.getFrequency());
        LocalDate today = LocalDate.now();
        LocalDate cursor = today;
        int streak = 0;

        for (int i = 0; i < MAX_LOOKBACK_DAYS; i++) {
            if (isScheduledOn(scheduledDays, cursor)) {
                if (done.contains(cursor)) {
                    streak++;
                } else if (!cursor.equals(today)) {
                    break; // ngày có lịch trong quá khứ mà bỏ lỡ -> đứt streak
                }
            }
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    /** null => có lịch mọi ngày (daily/weekly_*); ngược lại là tập thứ trong tuần (0=CN..6=T7). */
    private Set<Integer> parseScheduledDays(String frequency) {
        if (frequency == null || frequency.isBlank() || !frequency.contains("days")) {
            return null;
        }
        Matcher m = DAYS_ARRAY.matcher(frequency);
        if (!m.find()) {
            return null;
        }
        Set<Integer> days = new HashSet<>();
        for (String part : m.group(1).split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) {
                days.add(Integer.parseInt(t));
            }
        }
        return days.isEmpty() ? null : days;
    }

    private boolean isScheduledOn(Set<Integer> scheduledDays, LocalDate date) {
        if (scheduledDays == null) {
            return true; // daily / weekly_3 / weekly_5 -> mọi ngày đều có lịch
        }
        int jsDay = date.getDayOfWeek().getValue() % 7; // Mon(1)..Sun(7) -> Sun(0)..Sat(6)
        return scheduledDays.contains(jsDay);
    }
}
