package com.hehe.habit_tracker.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hehe.habit_tracker.dto.request.HabitCreationRequest;
import com.hehe.habit_tracker.dto.request.HabitUpdateRequest;
import com.hehe.habit_tracker.dto.response.HabitResponse;
import com.hehe.habit_tracker.entity.Habit;
import com.hehe.habit_tracker.entity.IconHabit;
import com.hehe.habit_tracker.entity.Users;
import com.hehe.habit_tracker.exception.AppException;
import com.hehe.habit_tracker.exception.ErrorCode;
import com.hehe.habit_tracker.mapper.HabitMapper;
import com.hehe.habit_tracker.repository.HabitRepository;
import com.hehe.habit_tracker.repository.IconHabitRepository;
import com.hehe.habit_tracker.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Mọi method nhận `username` (lấy từ claim 'sub' của JWT đã verify — xem HabitController),
 * KHÔNG nhận userId từ client. Habit là dữ liệu riêng tư của từng user:
 * - create: gán chủ sở hữu ngay lúc tạo.
 * - list : chỉ trả habit của chính user đó.
 * - get/update/delete: kiểm tra đúng chủ sở hữu, sai thì báo HABIT_NOT_FOUND (404,
 *   không phải 403) để không lộ ra rằng habit đó tồn tại nhưng thuộc người khác.
 *
 * icon/iconColor trong request là chuỗi phẳng (client không biết IconHabit là entity
 * riêng) — service tự tạo/cập nhật IconHabit tương ứng, ẩn chi tiết đó khỏi API.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HabitService {

    HabitRepository habitRepository;
    UserRepository userRepository;
    IconHabitRepository iconHabitRepository;
    HabitMapper habitMapper;

    public HabitResponse createHabit(HabitCreationRequest request, String username) {
        Users user = currentUser(username);

        Habit habit = habitMapper.toHabit(request);
        habit.setUser(user);
        applyIcon(habit, request.icon(), request.iconColor());
        return habitMapper.toHabitResponse(habitRepository.save(habit));
    }

    public List<HabitResponse> getAllHabits(String username) {
        Users user = currentUser(username);
        return habitRepository.findByUserId(user.getId())
                .stream()
                .map(habitMapper::toHabitResponse)
                .toList();
    }

    public HabitResponse getHabitById(Long id, String username) {
        Habit habit = ownedHabit(id, username);
        return habitMapper.toHabitResponse(habit);
    }

    public HabitResponse updateHabit(Long id, HabitUpdateRequest request, String username) {
        Habit habit = ownedHabit(id, username);
        habitMapper.updateHabit(habit, request);
        if (request.icon() != null || request.iconColor() != null) {
            applyIcon(habit, request.icon(), request.iconColor());
        }
        return habitMapper.toHabitResponse(habitRepository.save(habit));
    }

    public void deleteHabit(Long id, String username) {
        Habit habit = ownedHabit(id, username);
        habitRepository.delete(habit);
    }

    private Users currentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    /** Habit tồn tại và đúng của user này. Sai chủ -> coi như không tồn tại. */
    private Habit ownedHabit(Long id, String username) {
        Users user = currentUser(username);
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HABIT_NOT_FOUND));
        if (!habit.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.HABIT_NOT_FOUND);
        }
        return habit;
    }

    /**
     * Tạo mới hoặc cập nhật IconHabit gắn với habit này. Lưu IconHabit TRƯỚC (qua
     * repository riêng) để có id, vì Habit là owning side của quan hệ 1-1
     * (cột icon_id nằm trên bảng habits, trỏ tới bản ghi icons đã tồn tại).
     */
    private void applyIcon(Habit habit, String icon, String iconColor) {
        if (icon == null && iconColor == null) {
            return;
        }
        IconHabit iconHabit = habit.getIconHabit();
        if (iconHabit == null) {
            iconHabit = IconHabit.builder().isActived(true).build();
        }
        if (icon != null) {
            iconHabit.setIcon(icon);
        }
        if (iconColor != null) {
            iconHabit.setIconColor(iconColor);
        }
        habit.setIconHabit(iconHabitRepository.save(iconHabit));
    }
}
