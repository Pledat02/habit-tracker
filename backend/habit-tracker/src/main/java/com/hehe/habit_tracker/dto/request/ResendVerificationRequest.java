package com.hehe.habit_tracker.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Yêu cầu gửi lại email xác thực. */
public record ResendVerificationRequest(

        @NotBlank(message = "Email is required") @Email(message = "Invalid email") String email

) {
}
