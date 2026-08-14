package com.hehe.habit_tracker.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.hehe.habit_tracker.dto.request.HabitCreationRequest;
import com.hehe.habit_tracker.dto.request.HabitUpdateRequest;
import com.hehe.habit_tracker.dto.response.HabitResponse;
import com.hehe.habit_tracker.entity.Habit;

@Mapper(componentModel = "spring")
public interface HabitMapper {
    Habit toHabit(HabitCreationRequest request);

    @Mapping(source = "paused", target = "isPaused")
    HabitResponse toHabitResponse(Habit habit);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateHabit(@MappingTarget Habit habit, HabitUpdateRequest request);
}
