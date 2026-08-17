package com.hehe.habit_tracker.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hehe.habit_tracker.common.AchievementType;
import com.hehe.habit_tracker.dto.response.UserAchivementResponse;
import com.hehe.habit_tracker.entity.Achivement;
import com.hehe.habit_tracker.entity.Checkin;
import com.hehe.habit_tracker.entity.Habit;
import com.hehe.habit_tracker.entity.UserAchivement;
import com.hehe.habit_tracker.entity.Users;
import com.hehe.habit_tracker.mapper.UserAchivementMapper;
import com.hehe.habit_tracker.repository.AchivementRepository;
import com.hehe.habit_tracker.repository.CheckinRepository;
import com.hehe.habit_tracker.repository.HabitRepository;
import com.hehe.habit_tracker.repository.UserAchivementRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Tự động đánh giá & cấp thành tựu — thay cho việc cấp thủ công từ frontend.
 * Gọi ĐỒNG BỘ ngay sau mỗi check-in (xem CheckinService.createCheckin).
 *
 * Hai loại luật ở bản này (mỗi loại một handler, thêm loại mới = thêm 1 nhánh):
 * - STREAK (per-habit): habit vừa check-in đạt streak >= target ngày.
 * - MULTI_STREAK (account): >= target habit cùng đạt streak >= target2 ngày.
 *
 * Trả về danh sách thành tựu VỪA MỚI mở khoá ở lần này để frontend hiện chúc mừng.
 * Đã unlock trước đó thì bỏ qua (idempotent nhờ các exists... của repository).
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AchievementEngine {

    HabitRepository habitRepository;
    CheckinRepository checkinRepository;
    AchivementRepository achivementRepository;
    UserAchivementRepository userAchivementRepository;
    UserAchivementMapper userAchivementMapper;
    StreakCalculator streakCalculator;

    @Transactional
    public List<UserAchivementResponse> evaluate(Users user, Habit triggeredHabit) {
        // Tính streak hiện tại cho MỌI habit của user 1 lần (check-in vừa lưu đã nằm trong đây).
        List<Checkin> allCheckins = checkinRepository.findByHabitUserId(user.getId());
        Map<Long, List<LocalDate>> datesByHabit = allCheckins.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getHabit().getId(),
                        Collectors.mapping(Checkin::getCheckinDate, Collectors.toList())));

        List<Habit> habits = habitRepository.findByUserId(user.getId());
        Map<Long, Integer> streakByHabit = new HashMap<>();
        for (Habit h : habits) {
            streakByHabit.put(h.getId(),
                    streakCalculator.currentStreak(h, datesByHabit.getOrDefault(h.getId(), List.of())));
        }

        List<UserAchivement> newlyUnlocked = new ArrayList<>();

        // --- STREAK (per-habit): chỉ đánh giá đúng habit vừa check-in ---
        int triggeredStreak = streakByHabit.getOrDefault(triggeredHabit.getId(), 0);
        for (Achivement def : achivementRepository.findByTypeAndActiveTrue(AchievementType.STREAK)) {
            if (def.getTarget() != null
                    && triggeredStreak >= def.getTarget()
                    && !userAchivementRepository.existsByUserIdAndDefinitionIdAndHabitId(
                            user.getId(), def.getId(), triggeredHabit.getId())) {
                newlyUnlocked.add(grant(user, def, triggeredHabit));
            }
        }

        // --- MULTI_STREAK (account-level) ---
        for (Achivement def : achivementRepository.findByTypeAndActiveTrue(AchievementType.MULTI_STREAK)) {
            if (def.getTarget() == null) {
                continue;
            }
            int minDays = def.getTarget2() != null ? def.getTarget2() : 0;
            long habitsReaching = streakByHabit.values().stream().filter(s -> s >= minDays).count();
            if (habitsReaching >= def.getTarget()
                    && !userAchivementRepository.existsByUserIdAndDefinitionIdAndHabitIsNull(
                            user.getId(), def.getId())) {
                newlyUnlocked.add(grant(user, def, null)); // account-level -> habit = null
            }
        }

        return newlyUnlocked.stream().map(userAchivementMapper::toUserAchivementResponse).toList();
    }

    private UserAchivement grant(Users user, Achivement definition, Habit habit) {
        UserAchivement ua = UserAchivement.builder()
                .user(user)
                .definition(definition)
                .habit(habit)
                .build();
        return userAchivementRepository.save(ua);
    }
}
