package com.hehe.habit_tracker.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Payload trình duyệt gửi lên sau khi PushManager.subscribe() (giải phẳng từ PushSubscription của Web API). */
public record PushSubscribeRequest(
        @NotBlank String endpoint,
        @NotBlank String p256dh,
        @NotBlank String auth
) {
}
