package com.hehe.habit_tracker.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.hehe.habit_tracker.dto.request.AchivementCreationRequest;
import com.hehe.habit_tracker.dto.request.AchivementUpdateRequest;
import com.hehe.habit_tracker.dto.response.AchivementResponse;
import com.hehe.habit_tracker.entity.Achivement;

@Mapper(componentModel = "spring")
public interface AchivementMapper {

    Achivement toAchivement(AchivementCreationRequest request);

    AchivementResponse toAchivementResponse(Achivement achivement);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAchivement(@MappingTarget Achivement achivement, AchivementUpdateRequest request);
}
