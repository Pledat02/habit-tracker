package com.hehe.habit_tracker.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
 * frequency lưu dạng JSON: "daily" / "weekly_3" / "weekly_5" / {"type":"days","days":[0..6]}
 * (0=CN..6=T7). Hai chế độ tính streak (khớp frontend utils.ts):
 *  - "N lần/tuần" (weekly_3/weekly_5): đếm số TUẦN liên tiếp đạt đủ N check-in.
 *  - còn lại (daily / days cụ thể): đếm số NGÀY-có-lịch liên tiếp có check-in.
 * Parse thủ công (không dùng Jackson) vì tập giá trị nhỏ và cố định.
 */
@Component
public class StreakCalculator {

    private static final int MAX_LOOKBACK_DAYS = 730;
    private static final int MAX_LOOKBACK_WEEKS = 104; // ~2 năm, cận an toàn cho weekly
    // Bắt mảng số trong "days":[...] của frequency dạng {"type":"days","days":[1,3,5]}.
    private static final Pattern DAYS_ARRAY = Pattern.compile("\"days\"\\s*:\\s*\\[([0-9,\\s]*)]");

    public int currentStreak(Habit habit, List<LocalDate> checkinDates) {
        return currentStreak(habit, checkinDates, LocalDate.now());
    }

    /**
     * Overload nhận `today` tường minh — để test XÁC ĐỊNH, không phụ thuộc ngày chạy.
     * Production luôn gọi bản 2 tham số ở trên (LocalDate.now()).
     */
    int currentStreak(Habit habit, List<LocalDate> checkinDates, LocalDate today) {
        Integer weeklyTarget = parseWeeklyTarget(habit.getFrequency());
        if (weeklyTarget != null) {
            return currentWeeklyStreak(checkinDates, weeklyTarget, today);
        }

        Set<LocalDate> done = new HashSet<>(checkinDates);
        Set<Integer> scheduledDays = parseScheduledDays(habit.getFrequency());
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

    /**
     * Streak cho habit "N lần/tuần": số TUẦN liên tiếp đạt đủ >= N check-in, đếm ngược
     * từ tuần hiện tại. Tuần theo thứ Hai (ISO, khớp weekKey ở frontend). Tuần hiện tại
     * chưa đủ N nhưng CHƯA hết tuần thì không tính đứt (mirror "hôm nay chưa làm không đứt").
     */
    private int currentWeeklyStreak(List<LocalDate> checkinDates, int target, LocalDate today) {
        Map<LocalDate, Integer> perWeek = new HashMap<>();
        for (LocalDate d : checkinDates) {
            perWeek.merge(mondayOf(d), 1, Integer::sum);
        }
        LocalDate thisMonday = mondayOf(today);
        LocalDate cursor = thisMonday;
        int streak = 0;

        for (int i = 0; i < MAX_LOOKBACK_WEEKS; i++) {
            int count = perWeek.getOrDefault(cursor, 0);
            if (count >= target) {
                streak++;
            } else if (!cursor.equals(thisMonday)) {
                break; // tuần trong quá khứ không đủ số lần -> đứt streak
            }
            cursor = cursor.minusWeeks(1);
        }
        return streak;
    }

    /** Thứ Hai của tuần chứa `date` (ISO): Mon(1)..Sun(7) -> lùi về Mon. */
    private LocalDate mondayOf(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1L);
    }

    /** Target của habit "N lần/tuần": 3 cho weekly_3, 5 cho weekly_5; null nếu không phải. */
    private Integer parseWeeklyTarget(String frequency) {
        if (frequency == null) {
            return null;
        }
        if (frequency.contains("weekly_3")) {
            return 3;
        }
        if (frequency.contains("weekly_5")) {
            return 5;
        }
        return null;
    }

    /** Habit có lên lịch vào ngày này không (theo frequency). Dùng bởi ReminderScheduler. */
    public boolean isScheduledOn(String frequency, LocalDate date) {
        return isScheduledOn(parseScheduledDays(frequency), date);
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
