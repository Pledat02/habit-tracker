package com.hehe.habit_tracker.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreationRequest(
        // Không @NotBlank: form đăng ký của frontend chỉ thu tên/email/password, không có
        // trường username riêng -> để trống thì UserService tự sinh từ email.
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters") String username,
        @NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email,

        @NotBlank(message = "Password is required") @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters") String password,
        String role,
        /** IANA timezone của trình duyệt (frontend gửi). Null -> dùng default-timezone. */
        String timezone) {

}
