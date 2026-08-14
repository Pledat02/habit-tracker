package com.hehe.habit_tracker.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Email(message = "Email should be valid") String email,
        @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters") String password,
        String role
) {
}
