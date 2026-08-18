package com.hehe.habit_tracker.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hehe.habit_tracker.dto.request.UserAchivementCreationRequest;
import com.hehe.habit_tracker.dto.request.UserAchivementUpdateRequest;
import com.hehe.habit_tracker.dto.response.UserAchivementResponse;
import com.hehe.habit_tracker.entity.Achivement;
import com.hehe.habit_tracker.entity.Habit;
import com.hehe.habit_tracker.entity.UserAchivement;
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
 *
 * Mọi method nhận `userId` (từ claim JWT, xem UserAchivementController), không tra
 * bảng users. Trước đây grant() nhận thẳng userId trong BODY client (ai cũng cấp cho
 * ai được) — giờ userId luôn từ token đã verify.
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

    /** Cấp (mở khoá) một thành tựu cho CHÍNH user gọi request, có chặn trùng. */
    public UserAchivementResponse grant(UserAchivementCreationRequest request, Long userId) {
        Achivement definition = achivementRepository.findById(request.definitionId())
                .orElseThrow(() -> new AppException(ErrorCode.ACHIEVEMENT_NOT_FOUND));

        Habit habit = null;
        boolean duplicate;
        if (request.habitId() != null) {
            habit = habitRepository.findById(request.habitId())
                    .orElseThrow(() -> new AppException(ErrorCode.HABIT_NOT_FOUND));
            // Không cho gắn thành tựu vào habit của người khác (id proxy -> so miễn phí).
            if (!habit.getUser().getId().equals(userId)) {
                throw new AppException(ErrorCode.HABIT_NOT_FOUND);
            }
            duplicate = userAchivementRepository
                    .existsByUserIdAndDefinitionIdAndHabitId(userId, definition.getId(), habit.getId());
        } else {
            duplicate = userAchivementRepository
                    .existsByUserIdAndDefinitionIdAndHabitIsNull(userId, definition.getId());
        }
        if (duplicate) {
            throw new AppException(ErrorCode.USER_ACHIEVEMENT_EXISTED);
        }

        UserAchivement ua = UserAchivement.builder()
                .user(userRepository.getReferenceById(userId)) // proxy, không SELECT users
                .definition(definition)
                .habit(habit)
                .build();
        return userAchivementMapper.toUserAchivementResponse(userAchivementRepository.save(ua));
    }

    /** Tất cả thành tựu đã mở khoá của CHÍNH user gọi request. */
    public List<UserAchivementResponse> getMine(Long userId) {
        return userAchivementRepository.findByUserId(userId)
                .stream()
                .map(userAchivementMapper::toUserAchivementResponse)
                .toList();
    }

    public UserAchivementResponse getById(Long id, Long userId) {
        UserAchivement ua = ownedUserAchivement(id, userId);
        return userAchivementMapper.toUserAchivementResponse(ua);
    }

    public UserAchivementResponse update(Long id, UserAchivementUpdateRequest request, Long userId) {
        UserAchivement ua = ownedUserAchivement(id, userId);
        if (request.shared() != null) {
            ua.setShared(request.shared());
        }
        return userAchivementMapper.toUserAchivementResponse(userAchivementRepository.save(ua));
    }

    public void delete(Long id, Long userId) {
        UserAchivement ua = ownedUserAchivement(id, userId);
        userAchivementRepository.delete(ua);
    }

    /** Bản ghi mở khoá tồn tại và đúng của user này. Sai chủ -> coi như không tồn tại. */
    private UserAchivement ownedUserAchivement(Long id, Long userId) {
        UserAchivement ua = userAchivementRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_ACHIEVEMENT_NOT_FOUND));
        if (!ua.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.USER_ACHIEVEMENT_NOT_FOUND);
        }
        return ua;
    }
}
