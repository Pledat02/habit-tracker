package com.hehe.habit_tracker.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.hehe.habit_tracker.dto.request.UserCreationRequest;
import com.hehe.habit_tracker.dto.request.UserUpdateRequest;
import com.hehe.habit_tracker.dto.response.UserCreationResponse;
import com.hehe.habit_tracker.entity.Users;

@Mapper(componentModel = "spring")
public interface UserMapper {
    Users toUsers(UserCreationRequest request);

    UserCreationResponse toUserCreationResponse(Users user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(@MappingTarget Users user, UserUpdateRequest request);
}
