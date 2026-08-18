package com.hehe.habit_tracker.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Yêu cầu gửi link đặt lại mật khẩu tới email này. */
public record ForgotPasswordRequest(

        @NotBlank(message = "Email is required") @Email(message = "Invalid email") String email

) {
}
