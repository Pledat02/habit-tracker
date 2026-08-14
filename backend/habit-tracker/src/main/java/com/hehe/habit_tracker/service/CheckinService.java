package com.hehe.habit_tracker.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hehe.habit_tracker.dto.request.CheckinCreationRequest;
import com.hehe.habit_tracker.dto.request.CheckinUpdateRequest;
import com.hehe.habit_tracker.dto.response.CheckinResponse;
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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CheckinService {

    CheckinRepository checkinRepository;
    HabitRepository habitRepository;
    CheckinMapper checkinMapper;

    public CheckinResponse createCheckin(CheckinCreationRequest request) {
        Habit habit = habitRepository.findById(request.habitId())
                .orElseThrow(() -> new AppException(ErrorCode.HABIT_NOT_FOUND));

        LocalDate date = request.checkinDate() != null ? request.checkinDate() : LocalDate.now();

        if (checkinRepository.existsByHabitIdAndCheckinDate(habit.getId(), date)) {
            throw new AppException(ErrorCode.CHECKIN_EXISTED);
        }

        Checkin checkin = new Checkin();
        checkin.setHabit(habit);
        checkin.setCheckinDate(date);
        checkin.setNote(request.note());
        return checkinMapper.toCheckinResponse(checkinRepository.save(checkin));
    }

    public List<CheckinResponse> getCheckinsByHabit(Long habitId) {
        if (!habitRepository.existsById(habitId)) {
            throw new AppException(ErrorCode.HABIT_NOT_FOUND);
        }
        return checkinRepository.findByHabitId(habitId)
                .stream()
                .map(checkinMapper::toCheckinResponse)
                .toList();
    }

    public CheckinResponse getCheckinById(Long id) {
        Checkin checkin = checkinRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_NOT_FOUND));
        return checkinMapper.toCheckinResponse(checkin);
    }

    public CheckinResponse updateCheckin(Long id, CheckinUpdateRequest request) {
        Checkin checkin = checkinRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_NOT_FOUND));
        if (request.note() != null) {
            checkin.setNote(request.note());
        }
        return checkinMapper.toCheckinResponse(checkinRepository.save(checkin));
    }

    public void deleteCheckin(Long id) {
        if (!checkinRepository.existsById(id)) {
            throw new AppException(ErrorCode.CHECKIN_NOT_FOUND);
        }
        checkinRepository.deleteById(id);
    }
}
