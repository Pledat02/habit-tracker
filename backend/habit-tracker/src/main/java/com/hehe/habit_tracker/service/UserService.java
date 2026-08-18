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
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Username không bắt buộc từ client (form frontend không thu trường này) ->
        // tự sinh từ phần trước '@' của email khi bỏ trống.
        String username = (request.username() == null || request.username().isBlank())
                ? generateUniqueUsername(emailLocalPart(request.email()))
                : request.username();
        if (userRepository.existsByUsername(username)) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Dựng lại request với username đã chốt (KHÔNG thể gọi mapper trực tiếp với
        // username null: Users.username là @NonNull, mapper sẽ ném NPE ngay khi tạo).
        Users user = userMapper.toUsers(
                new UserCreationRequest(username, request.email(), request.password(), request.role(),
                        request.timezone()));
        user.setPassword(passwordEncoder.encode(request.password()));
        if (request.timezone() != null && !request.timezone().isBlank()) {
            user.setZoneId(request.timezone());
        }
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

    /** Dùng cho GET /users/me — username lấy từ claim 'sub' của JWT đã xác thực (KHÔNG nhận từ client). */
    public UserCreationResponse getCurrentUser(String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserCreationResponse(user);
    }

    /** Tự sinh username duy nhất từ 1 chuỗi gợi ý (email, tên...) — dùng khi không có username thật. */
    public String generateUniqueUsername(String seed) {
        String base = seed.replaceAll("[^a-zA-Z0-9._-]", "").toLowerCase();
        if (base.isBlank()) {
            base = "user";
        }
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }

    private String emailLocalPart(String email) {
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
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
