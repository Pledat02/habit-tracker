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
 *
 * Mọi method nhận `username` (từ JWT, xem UserAchivementController), không tin
 * userId từ client — trước đây grant() nhận thẳng userId trong body, nghĩa là
 * bất kỳ user nào cũng cấp được thành tựu cho bất kỳ ai khác.
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
    public UserAchivementResponse grant(UserAchivementCreationRequest request, String username) {
        Users user = currentUser(username);

        Achivement definition = achivementRepository.findById(request.definitionId())
                .orElseThrow(() -> new AppException(ErrorCode.ACHIEVEMENT_NOT_FOUND));

        Habit habit = null;
        boolean duplicate;
        if (request.habitId() != null) {
            habit = habitRepository.findById(request.habitId())
                    .orElseThrow(() -> new AppException(ErrorCode.HABIT_NOT_FOUND));
            // Không cho gắn thành tựu vào habit của người khác.
            if (!habit.getUser().getId().equals(user.getId())) {
                throw new AppException(ErrorCode.HABIT_NOT_FOUND);
            }
            duplicate = userAchivementRepository
                    .existsByUserIdAndDefinitionIdAndHabitId(user.getId(), definition.getId(), habit.getId());
        } else {
            duplicate = userAchivementRepository
                    .existsByUserIdAndDefinitionIdAndHabitIsNull(user.getId(), definition.getId());
        }
        if (duplicate) {
            throw new AppException(ErrorCode.USER_ACHIEVEMENT_EXISTED);
        }

        UserAchivement ua = UserAchivement.builder()
                .user(user)
                .definition(definition)
                .habit(habit)
                .build();
        return userAchivementMapper.toUserAchivementResponse(userAchivementRepository.save(ua));
    }

    /** Tất cả thành tựu đã mở khoá của CHÍNH user gọi request. */
    public List<UserAchivementResponse> getMine(String username) {
        Users user = currentUser(username);
        return userAchivementRepository.findByUserId(user.getId())
                .stream()
                .map(userAchivementMapper::toUserAchivementResponse)
                .toList();
    }

    public UserAchivementResponse getById(Long id, String username) {
        UserAchivement ua = ownedUserAchivement(id, username);
        return userAchivementMapper.toUserAchivementResponse(ua);
    }

    public UserAchivementResponse update(Long id, UserAchivementUpdateRequest request, String username) {
        UserAchivement ua = ownedUserAchivement(id, username);
        if (request.shared() != null) {
            ua.setShared(request.shared());
        }
        return userAchivementMapper.toUserAchivementResponse(userAchivementRepository.save(ua));
    }

    public void delete(Long id, String username) {
        UserAchivement ua = ownedUserAchivement(id, username);
        userAchivementRepository.delete(ua);
    }

    private Users currentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    /** Bản ghi mở khoá tồn tại và đúng của user này. Sai chủ -> coi như không tồn tại. */
    private UserAchivement ownedUserAchivement(Long id, String username) {
        Users user = currentUser(username);
        UserAchivement ua = userAchivementRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_ACHIEVEMENT_NOT_FOUND));
        if (!ua.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.USER_ACHIEVEMENT_NOT_FOUND);
        }
        return ua;
    }
}
