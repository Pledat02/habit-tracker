package com.hehe.habit_tracker.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.hehe.habit_tracker.dto.request.IconCreationRequest;
import com.hehe.habit_tracker.dto.request.IconUpdateRequest;
import com.hehe.habit_tracker.dto.response.IconResponse;
import com.hehe.habit_tracker.entity.IconHabit;

@Mapper(componentModel = "spring")
public interface IconMapper {
    IconHabit toIconHabit(IconCreationRequest request);

    @Mapping(source = "actived", target = "isActived")
    IconResponse toIconResponse(IconHabit iconHabit);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateIconHabit(@MappingTarget IconHabit iconHabit, IconUpdateRequest request);
}
