package com.hehe.habit_tracker.dto.response;

import java.time.Instant;

public record UserCreationResponse(Long id, String username, String email, String role,
        Instant createdAt,
        Instant updatedAt) {
}