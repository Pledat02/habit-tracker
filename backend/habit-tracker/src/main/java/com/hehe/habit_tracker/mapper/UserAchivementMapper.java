package com.hehe.habit_tracker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hehe.habit_tracker.dto.response.UserAchivementResponse;
import com.hehe.habit_tracker.entity.UserAchivement;

@Mapper(componentModel = "spring")
public interface UserAchivementMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "definition.id", target = "definitionId")
    @Mapping(source = "definition.code", target = "code")
    @Mapping(source = "definition.name", target = "name")
    @Mapping(source = "definition.icon", target = "icon")
    @Mapping(source = "definition.type", target = "type")
    @Mapping(source = "definition.category", target = "category")
    @Mapping(source = "habit.id", target = "habitId")
    UserAchivementResponse toUserAchivementResponse(UserAchivement userAchivement);
}
