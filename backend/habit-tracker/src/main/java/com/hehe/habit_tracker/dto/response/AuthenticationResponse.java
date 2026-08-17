package com.hehe.habit_tracker.dto.response;

import lombok.Builder;

@Builder
public record AuthenticationResponse(
        String token,
        boolean authenticated
) {
}
