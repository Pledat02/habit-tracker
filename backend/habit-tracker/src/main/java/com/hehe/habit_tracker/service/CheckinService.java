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
import com.hehe.habit_tracker.entity.Users;
import com.hehe.habit_tracker.exception.AppException;
import com.hehe.habit_tracker.exception.ErrorCode;
import com.hehe.habit_tracker.mapper.CheckinMapper;
import com.hehe.habit_tracker.repository.CheckinRepository;
import com.hehe.habit_tracker.repository.HabitRepository;
import com.hehe.habit_tracker.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Checkin không có field user trực tiếp — chủ sở hữu suy ra qua checkin.habit.user.
 * Mọi method nhận `username` (từ JWT, xem CheckinController), không tin habitId
 * bất kỳ do client gửi: phải verify habit đó thuộc đúng người gọi trước khi thao tác.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CheckinService {

    CheckinRepository checkinRepository;
    HabitRepository habitRepository;
    UserRepository userRepository;
    CheckinMapper checkinMapper;
    AchievementEngine achievementEngine;

    public CheckinResultResponse createCheckin(CheckinCreationRequest request, String username) {
        Habit habit = ownedHabit(request.habitId(), username);

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
    public List<CheckinResponse> getAllForUser(String username) {
        Users user = currentUser(username);
        return checkinRepository.findByHabitUserId(user.getId())
                .stream()
                .map(checkinMapper::toCheckinResponse)
                .toList();
    }

    public List<CheckinResponse> getCheckinsByHabit(Long habitId, String username) {
        ownedHabit(habitId, username); // ném lỗi nếu habit không tồn tại hoặc không phải của user này
        return checkinRepository.findByHabitId(habitId)
                .stream()
                .map(checkinMapper::toCheckinResponse)
                .toList();
    }

    public CheckinResponse getCheckinById(Long id, String username) {
        Checkin checkin = ownedCheckin(id, username);
        return checkinMapper.toCheckinResponse(checkin);
    }

    public CheckinResponse updateCheckin(Long id, CheckinUpdateRequest request, String username) {
        Checkin checkin = ownedCheckin(id, username);
        if (request.note() != null) {
            checkin.setNote(request.note());
        }
        return checkinMapper.toCheckinResponse(checkinRepository.save(checkin));
    }

    public void deleteCheckin(Long id, String username) {
        Checkin checkin = ownedCheckin(id, username);
        checkinRepository.delete(checkin);
    }

    private Users currentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    /** Habit tồn tại và đúng của user này. Sai chủ -> coi như không tồn tại (404, không phải 403). */
    private Habit ownedHabit(Long habitId, String username) {
        Users user = currentUser(username);
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new AppException(ErrorCode.HABIT_NOT_FOUND));
        if (!habit.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.HABIT_NOT_FOUND);
        }
        return habit;
    }

    private Checkin ownedCheckin(Long id, String username) {
        Users user = currentUser(username);
        Checkin checkin = checkinRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_NOT_FOUND));
        if (!checkin.getHabit().getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.CHECKIN_NOT_FOUND);
        }
        return checkin;
    }
}
