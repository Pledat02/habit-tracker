package com.hehe.habit_tracker.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hehe.habit_tracker.common.ApiResponse;
import com.hehe.habit_tracker.common.BaseController;
import com.hehe.habit_tracker.dto.request.PushSubscribeRequest;
import com.hehe.habit_tracker.entity.PushSubscription;
import com.hehe.habit_tracker.repository.PushSubscriptionRepository;
import com.hehe.habit_tracker.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Quản lý Web Push subscription của user hiện tại (xác định qua JWT). */
@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
public class PushController extends BaseController<Void> {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final UserRepository userRepository;

    @Value("${app.push.vapid-public-key:}")
    private String vapidPublicKey;

    /** Client cần public key để PushManager.subscribe(). */
    @GetMapping("/public-key")
    public ApiResponse<String> publicKey() {
        return ApiResponse.success(vapidPublicKey, 200);
    }

    /** Lưu (hoặc cập nhật) subscription theo endpoint cho user đang gọi. */
    @PostMapping("/subscribe")
    public ApiResponse<Void> subscribe(@Valid @RequestBody PushSubscribeRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = currentUserId(jwt);
        PushSubscription sub = pushSubscriptionRepository.findByEndpoint(request.endpoint())
                .orElseGet(PushSubscription::new);
        sub.setUser(userRepository.getReferenceById(userId));
        sub.setEndpoint(request.endpoint());
        sub.setP256dh(request.p256dh());
        sub.setAuth(request.auth());
        pushSubscriptionRepository.save(sub);
        return ApiResponse.success(null, 201);
    }

    @DeleteMapping("/subscribe")
    public ApiResponse<Void> unsubscribe(@RequestParam String endpoint) {
        pushSubscriptionRepository.findByEndpoint(endpoint)
                .ifPresent(pushSubscriptionRepository::delete);
        return ApiResponse.success(null, 204);
    }
}
