package com.hehe.habit_tracker.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hehe.habit_tracker.dto.request.UserAchivementCreationRequest;
import com.hehe.habit_tracker.dto.request.UserAchivementUpdateRequest;
import com.hehe.habit_tracker.dto.response.UserAchivementResponse;
import com.hehe.habit_tracker.entity.Achivement;
import com.hehe.habit_tracker.entity.Habit;
import com.hehe.habit_tracker.entity.UserAchivement;
import com.hehe.habit_tracker.entity.Users;
import com.hehe.habit_tracker.exception.AppException;
import com.hehe.habit_tracker.exception.ErrorCode;
import com.hehe.habit_tracker.mapper.UserAchivementMapper;
import com.hehe.habit_tracker.repository.AchivementRepository;
import com.hehe.habit_tracker.repository.HabitRepository;
import com.hehe.habit_tracker.repository.UserAchivementRepository;
import com.hehe.habit_tracker.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Quản lý bản ghi mở khoá thành tựu (cấp thủ công + đọc).
 * Việc TỰ ĐỘNG đánh giá điều kiện để cấp (unlock engine) làm sau.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserAchivementService {

    UserAchivementRepository userAchivementRepository;
    UserRepository userRepository;
    AchivementRepository achivementRepository;
    HabitRepository habitRepository;
    UserAchivementMapper userAchivementMapper;

    /** Cấp (mở khoá) một thành tựu cho user, có chặn trùng. */
    public UserAchivementResponse grant(UserAchivementCreationRequest request) {
        Users user = userRepository.findById(request.userId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Achivement definition = achivementRepository.findById(request.definitionId())
                .orElseThrow(() -> new AppException(ErrorCode.ACHIEVEMENT_NOT_FOUND));

        Habit habit = null;
        boolean duplicate;
        if (request.habitId() != null) {
            habit = habitRepository.findById(request.habitId())
                    .orElseThrow(() -> new AppException(ErrorCode.HABIT_NOT_FOUND));
            duplicate = userAchivementRepository
                    .existsByUserIdAndDefinitionIdAndHabitId(user.getId(), definition.getId(), habit.getId());
        } else {
            duplicate = userAchivementRepository
                    .existsByUserIdAndDefinitionIdAndHabitIsNull(user.getId(), definition.getId());
        }
        if (duplicate) {
            throw new AppException(ErrorCode.USER_ACHIEVEMENT_EXISTED);
        }

        UserAchivement ua = new UserAchivement();
        ua.setUser(user);
        ua.setDefinition(definition);
        ua.setHabit(habit);
        return userAchivementMapper.toUserAchivementResponse(userAchivementRepository.save(ua));
    }

    /** Tất cả thành tựu đã mở khoá của 1 user. */
    public List<UserAchivementResponse> getByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        return userAchivementRepository.findByUserId(userId)
                .stream()
                .map(userAchivementMapper::toUserAchivementResponse)
                .toList();
    }

    public UserAchivementResponse getById(Long id) {
        UserAchivement ua = userAchivementRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_ACHIEVEMENT_NOT_FOUND));
        return userAchivementMapper.toUserAchivementResponse(ua);
    }

    public UserAchivementResponse update(Long id, UserAchivementUpdateRequest request) {
        UserAchivement ua = userAchivementRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_ACHIEVEMENT_NOT_FOUND));
        if (request.shared() != null) {
            ua.setShared(request.shared());
        }
        return userAchivementMapper.toUserAchivementResponse(userAchivementRepository.save(ua));
    }

    public void delete(Long id) {
        if (!userAchivementRepository.existsById(id)) {
            throw new AppException(ErrorCode.USER_ACHIEVEMENT_NOT_FOUND);
        }
        userAchivementRepository.deleteById(id);
    }
}
