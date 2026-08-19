package com.hehe.habit_tracker.service;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * Chuyển frequency của habit sang chuỗi RRULE (RFC 5545) mà Google Calendar dùng trong
 * trường {@code recurrence}. Logic thuần, không phụ thuộc I/O -> test xác định được.
 *
 * frequency: "daily" / "weekly_3" / "weekly_5" / {"type":"days","days":[0..6]} (0=CN..6=T7).
 */
@Component
public class RRuleBuilder {

    private static final Pattern DAYS_ARRAY = Pattern.compile("\"days\"\\s*:\\s*\\[([0-9,\\s]*)]");
    // Index theo quy ước JS 0=CN..6=T7 -> mã ngày RRULE.
    private static final String[] RRULE_DAYS = {"SU", "MO", "TU", "WE", "TH", "FR", "SA"};

    /** RRULE cho habit. Bao gồm tiền tố "RRULE:" đúng như Google Calendar yêu cầu. */
    public String toRRule(String frequency) {
        if (frequency == null || frequency.isBlank()) {
            return "RRULE:FREQ=DAILY";
        }
        // "N lần/tuần" linh hoạt, không gắn thứ cố định -> nhắc mỗi ngày (title ghi rõ N lần/tuần).
        if (frequency.contains("weekly_3") || frequency.contains("weekly_5")) {
            return "RRULE:FREQ=DAILY";
        }
        Set<Integer> days = parseDays(frequency);
        if (days == null || days.isEmpty()) {
            return "RRULE:FREQ=DAILY";
        }
        String byday = new TreeSet<>(days).stream()
                .filter(d -> d >= 0 && d <= 6)
                .map(d -> RRULE_DAYS[d])
                .collect(Collectors.joining(","));
        return byday.isEmpty() ? "RRULE:FREQ=DAILY" : "RRULE:FREQ=WEEKLY;BYDAY=" + byday;
    }

    private Set<Integer> parseDays(String frequency) {
        if (!frequency.contains("days")) {
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
        return days;
    }
}
