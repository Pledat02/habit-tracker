package com.hehe.habit_tracker.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hehe.habit_tracker.entity.Habit;
import com.hehe.habit_tracker.entity.PushSubscription;
import com.hehe.habit_tracker.repository.CheckinRepository;
import com.hehe.habit_tracker.repository.HabitRepository;
import com.hehe.habit_tracker.repository.PushSubscriptionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Chạy mỗi phút: gửi Web Push nhắc nhở cho habit tới GIỜ NHẮC (theo timezone user),
 * ĐANG lên lịch hôm nay, và CHƯA check-in hôm nay.
 *
 * Idempotency đơn giản: cron chạy 1 lần/phút, remindTime chính xác tới phút -> mỗi habit
 * chỉ khớp đúng 1 phút/ngày. (Nếu cần chắc chắn tuyệt đối khi server restart giữa phút đó,
 * sau này thêm bảng log "đã gửi (habit_id, ngày)".)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReminderScheduler {

    private final HabitRepository habitRepository;
    private final CheckinRepository checkinRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final WebPushService webPushService;
    private final StreakCalculator streakCalculator;

    @Value("${app.default-timezone:UTC}")
    private String defaultTimezone;

    @Scheduled(cron = "0 * * * * *") // đầu mỗi phút
    @Transactional
    public void sendDueReminders() {
        if (!webPushService.isEnabled()) {
            return; // chưa cấu hình VAPID -> bỏ qua
        }

        for (Habit habit : habitRepository.findRemindable()) {
            ZoneId zone = resolveZone(habit.getUser().getZoneId());
            LocalTime nowMinute = LocalTime.now(zone).truncatedTo(ChronoUnit.MINUTES);
            if (!nowMinute.equals(habit.getRemindTime().truncatedTo(ChronoUnit.MINUTES))) {
                continue; // chưa tới giờ nhắc (theo giờ user)
            }

            LocalDate today = LocalDate.now(zone);
            if (!streakCalculator.isScheduledOn(habit.getFrequency(), today)) {
                continue; // hôm nay không lên lịch habit này
            }
            if (checkinRepository.existsByHabitIdAndCheckinDate(habit.getId(), today)) {
                continue; // đã check-in hôm nay -> khỏi nhắc
            }

            List<PushSubscription> subs = pushSubscriptionRepository.findByUserId(habit.getUser().getId());
            for (PushSubscription sub : subs) {
                webPushService.send(sub, "Nhắc nhở: " + habit.getName(),
                        "Đến giờ rồi! Check-in ngay để giữ streak nhé 💪", "/");
            }
            if (!subs.isEmpty()) {
                log.info("Đã gửi nhắc nhở habit '{}' cho user {}", habit.getName(), habit.getUser().getId());
            }
        }
    }

    private ZoneId resolveZone(String zoneId) {
        try {
            return ZoneId.of(zoneId != null ? zoneId : defaultTimezone);
        } catch (Exception e) {
            return ZoneId.of(defaultTimezone);
        }
    }
}
