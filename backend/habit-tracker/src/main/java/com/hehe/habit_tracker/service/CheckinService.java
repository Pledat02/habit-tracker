package com.hehe.habit_tracker.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hehe.habit_tracker.dto.request.CheckinCreationRequest;
import com.hehe.habit_tracker.dto.request.CheckinUpdateRequest;
import com.hehe.habit_tracker.dto.response.CheckinResponse;
import com.hehe.habit_tracker.dto.response.CheckinResultResponse;
import com.hehe.habit_tracker.entity.Checkin;
import com.hehe.habit_tracker.entity.Habit;
import com.hehe.habit_tracker.exception.AppException;
import com.hehe.habit_tracker.exception.ErrorCode;
import com.hehe.habit_tracker.mapper.CheckinMapper;
import com.hehe.habit_tracker.repository.CheckinRepository;
import com.hehe.habit_tracker.repository.HabitRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Checkin không có field user trực tiếp — chủ sở hữu suy ra qua checkin.habit.user.
 * Mọi method nhận `userId` (từ claim JWT, xem CheckinController), không tra bảng users
 * và không tin habitId client gửi mù: phải verify habit thuộc đúng người gọi trước khi thao tác.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CheckinService {

    CheckinRepository checkinRepository;
    HabitRepository habitRepository;
    CheckinMapper checkinMapper;
    AchievementEngine achievementEngine;

    public CheckinResultResponse createCheckin(CheckinCreationRequest request, Long userId) {
        Habit habit = ownedHabit(request.habitId(), userId);

        LocalDate date = request.checkinDate() != null ? request.checkinDate() : LocalDate.now();
        if (checkinRepository.existsByHabitIdAndCheckinDate(habit.getId(), date)) {
            throw new AppException(ErrorCode.CHECKIN_EXISTED);
        }

        Checkin checkin = Checkin.builder()
                .habit(habit)
                .checkinDate(date)
                .note(request.note())
                .build();
        CheckinResponse saved = checkinMapper.toCheckinResponse(checkinRepository.save(checkin));

        // Sau khi lưu, đánh giá & tự cấp thành tựu (engine đọc luôn check-in vừa lưu).
        var newAchievements = achievementEngine.evaluate(habit.getUser(), habit);

        return CheckinResultResponse.builder()
                .checkin(saved)
                .newAchievements(newAchievements)
                .build();
    }

    /** Tất cả check-in thuộc mọi habit của user này (Dashboard/Insights cần gộp toàn bộ). */
    public List<CheckinResponse> getAllForUser(Long userId) {
        return checkinRepository.findByHabitUserId(userId)
                .stream()
                .map(checkinMapper::toCheckinResponse)
                .toList();
    }

    public List<CheckinResponse> getCheckinsByHabit(Long habitId, Long userId) {
        ownedHabit(habitId, userId); // ném lỗi nếu habit không tồn tại hoặc không phải của user này
        return checkinRepository.findByHabitId(habitId)
                .stream()
                .map(checkinMapper::toCheckinResponse)
                .toList();
    }

    public CheckinResponse getCheckinById(Long id, Long userId) {
        Checkin checkin = ownedCheckin(id, userId);
        return checkinMapper.toCheckinResponse(checkin);
    }

    public CheckinResponse updateCheckin(Long id, CheckinUpdateRequest request, Long userId) {
        Checkin checkin = ownedCheckin(id, userId);
        if (request.note() != null) {
            checkin.setNote(request.note());
        }
        return checkinMapper.toCheckinResponse(checkinRepository.save(checkin));
    }

    public void deleteCheckin(Long id, Long userId) {
        Checkin checkin = ownedCheckin(id, userId);
        checkinRepository.delete(checkin);
    }

    /** Habit tồn tại và đúng của user này. So habit.getUser().getId() (id proxy, miễn phí) với userId. */
    private Habit ownedHabit(Long habitId, Long userId) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new AppException(ErrorCode.HABIT_NOT_FOUND));
        if (!habit.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.HABIT_NOT_FOUND);
        }
        return habit;
    }

    private Checkin ownedCheckin(Long id, Long userId) {
        Checkin checkin = checkinRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_NOT_FOUND));
        if (!checkin.getHabit().getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.CHECKIN_NOT_FOUND);
        }
        return checkin;
    }
}
