package com.hehe.habit_tracker.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Xác thực email bằng token nhận qua email. */
public record VerifyEmailRequest(

        @NotBlank(message = "Token is required") String token

) {
}
