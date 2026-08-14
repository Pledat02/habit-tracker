package com.hehe.habit_tracker.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hehe.habit_tracker.dto.request.HabitCreationRequest;
import com.hehe.habit_tracker.dto.request.HabitUpdateRequest;
import com.hehe.habit_tracker.dto.response.HabitResponse;
import com.hehe.habit_tracker.entity.Habit;
import com.hehe.habit_tracker.exception.AppException;
import com.hehe.habit_tracker.exception.ErrorCode;
import com.hehe.habit_tracker.mapper.HabitMapper;
import com.hehe.habit_tracker.repository.HabitRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HabitService {

    HabitRepository habitRepository;
    HabitMapper habitMapper;

    public HabitResponse createHabit(HabitCreationRequest request) {
        Habit habit = habitMapper.toHabit(request);
        return habitMapper.toHabitResponse(habitRepository.save(habit));
    }

    public List<HabitResponse> getAllHabits() {
        return habitRepository.findAll()
                .stream()
                .map(habitMapper::toHabitResponse)
                .toList();
    }

    public HabitResponse getHabitById(Long id) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HABIT_NOT_FOUND));
        return habitMapper.toHabitResponse(habit);
    }

    public HabitResponse updateHabit(Long id, HabitUpdateRequest request) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HABIT_NOT_FOUND));
        habitMapper.updateHabit(habit, request);
        return habitMapper.toHabitResponse(habitRepository.save(habit));
    }

    public void deleteHabit(Long id) {
        if (!habitRepository.existsById(id)) {
            throw new AppException(ErrorCode.HABIT_NOT_FOUND);
        }
        habitRepository.deleteById(id);
    }
}
