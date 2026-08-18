package com.hehe.habit_tracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Đặt lại mật khẩu bằng token nhận qua email. */
public record ResetPasswordRequest(

        @NotBlank(message = "Token is required") String token,
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters") String newPassword

) {
}
