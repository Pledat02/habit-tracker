package com.hehe.habit_tracker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hehe.habit_tracker.dto.response.CheckinResponse;
import com.hehe.habit_tracker.entity.Checkin;

@Mapper(componentModel = "spring")
public interface CheckinMapper {

    @Mapping(source = "habit.id", target = "habitId")
    CheckinResponse toCheckinResponse(Checkin checkin);
}
