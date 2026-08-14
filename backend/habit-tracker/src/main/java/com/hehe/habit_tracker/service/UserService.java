package com.hehe.habit_tracker.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hehe.habit_tracker.common.Role;
import com.hehe.habit_tracker.dto.request.UserCreationRequest;
import com.hehe.habit_tracker.dto.request.UserUpdateRequest;
import com.hehe.habit_tracker.dto.response.UserCreationResponse;
import com.hehe.habit_tracker.entity.Users;
import com.hehe.habit_tracker.exception.AppException;
import com.hehe.habit_tracker.exception.ErrorCode;
import com.hehe.habit_tracker.mapper.UserMapper;
import com.hehe.habit_tracker.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserCreationResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        Users user = userMapper.toUsers(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        if (request.role() != null && !request.role().isBlank()) {
            try {
                user.setRole(Role.valueOf(request.role().toUpperCase()));
            } catch (IllegalArgumentException e) {
                user.setRole(Role.USER);
            }
        } else {
            user.setRole(Role.USER);
        }
        return userMapper.toUserCreationResponse(userRepository.save(user));
    }

    public List<UserCreationResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserCreationResponse)
                .toList();
    }

    public UserCreationResponse getUserById(Long id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserCreationResponse(user);
    }

    public UserCreationResponse updateUser(Long id, UserUpdateRequest request) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.role() != null && !request.role().isBlank()) {
            try {
                user.setRole(Role.valueOf(request.role().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid role
            }
        }
        return userMapper.toUserCreationResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        userRepository.deleteById(id);
    }
}
