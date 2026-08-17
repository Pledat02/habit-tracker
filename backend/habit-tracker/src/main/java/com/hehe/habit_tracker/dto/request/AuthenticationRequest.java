package com.hehe.habit_tracker.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(

        @NotBlank(message = "username is required") String username,
        @NotBlank(message = "password is required") String password

) {
}
