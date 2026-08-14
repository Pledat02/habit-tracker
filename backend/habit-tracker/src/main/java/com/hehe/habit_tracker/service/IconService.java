package com.hehe.habit_tracker.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hehe.habit_tracker.dto.request.IconCreationRequest;
import com.hehe.habit_tracker.dto.request.IconUpdateRequest;
import com.hehe.habit_tracker.dto.response.IconResponse;
import com.hehe.habit_tracker.entity.IconHabit;
import com.hehe.habit_tracker.exception.AppException;
import com.hehe.habit_tracker.exception.ErrorCode;
import com.hehe.habit_tracker.mapper.IconMapper;
import com.hehe.habit_tracker.repository.IconHabitRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IconService {

    IconHabitRepository iconHabitRepository;
    IconMapper iconMapper;

    public IconResponse createIcon(IconCreationRequest request) {
        IconHabit iconHabit = iconMapper.toIconHabit(request);
        return iconMapper.toIconResponse(iconHabitRepository.save(iconHabit));
    }

    public List<IconResponse> getAllIcons() {
        return iconHabitRepository.findAll()
                .stream()
                .map(iconMapper::toIconResponse)
                .toList();
    }

    public IconResponse getIconById(Long id) {
        IconHabit iconHabit = iconHabitRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ICON_NOT_FOUND));
        return iconMapper.toIconResponse(iconHabit);
    }

    public IconResponse updateIcon(Long id, IconUpdateRequest request) {
        IconHabit iconHabit = iconHabitRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ICON_NOT_FOUND));
        iconMapper.updateIconHabit(iconHabit, request);
        return iconMapper.toIconResponse(iconHabitRepository.save(iconHabit));
    }

    public void deleteIcon(Long id) {
        if (!iconHabitRepository.existsById(id)) {
            throw new AppException(ErrorCode.ICON_NOT_FOUND);
        }
        iconHabitRepository.deleteById(id);
    }
}
