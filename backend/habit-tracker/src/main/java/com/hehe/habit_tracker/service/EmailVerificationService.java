package com.hehe.habit_tracker.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hehe.habit_tracker.entity.EmailVerificationToken;
import com.hehe.habit_tracker.entity.Users;
import com.hehe.habit_tracker.exception.AppException;
import com.hehe.habit_tracker.exception.ErrorCode;
import com.hehe.habit_tracker.repository.EmailVerificationTokenRepository;
import com.hehe.habit_tracker.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

/**
 * Xác thực email khi đăng ký. KHÔNG chặn đăng nhập — chỉ đánh dấu email đã xác thực để
 * hiển thị/nhắc trên UI. Token chỉ lưu HASH, dùng 1 lần, có hạn (giống reset).
 */
@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailVerificationService {

    UserRepository userRepository;
    EmailVerificationTokenRepository tokenRepository;
    EmailService emailService;
    SecureRandom secureRandom = new SecureRandom();

    @Value("${app.email-verification.token-ttl-seconds:86400}") // 24 giờ
    @NonFinal
    long ttlSeconds;

    @Value("${app.email-verification.link-base:${app.frontend-origin}/verify-email}")
    @NonFinal
    String linkBase;

    /** Sinh token + gửi email xác thực. Gọi ngay sau khi tạo user (best-effort, không chặn đăng ký). */
    @Transactional
    public void sendVerification(Users user) {
        if (user.isEmailVerified() || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }
        tokenRepository.markAllUsedForUser(user.getId());

        String rawToken = randomToken();
        tokenRepository.save(EmailVerificationToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .expiresAt(Instant.now().plusSeconds(ttlSeconds))
                .used(false)
                .build());

        String link = linkBase + "?token=" + rawToken;
        emailService.send(user.getEmail(), "Xác thực email Habit Tracker",
                "Chào mừng bạn đến với Habit Tracker! Bấm vào link sau để xác thực email (hết hạn sau "
                        + (ttlSeconds / 3600) + " giờ):\n\n" + link
                        + "\n\nNếu không phải bạn đăng ký, hãy bỏ qua email này.");
    }

    /** Gửi lại link xác thực theo email. Luôn trả như nhau (không lộ email tồn tại hay không). */
    @Transactional
    public void resend(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!user.isEmailVerified()) {
                sendVerification(user);
            }
        });
    }

    /** Xác thực bằng token. Sai/hết hạn/đã dùng -> VERIFICATION_TOKEN_INVALID. */
    @Transactional
    public void verify(String rawToken) {
        EmailVerificationToken token = tokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new AppException(ErrorCode.VERIFICATION_TOKEN_INVALID));
        if (token.isUsed() || token.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.VERIFICATION_TOKEN_INVALID);
        }
        Users user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        token.setUsed(true);
        tokenRepository.save(token);
    }

    @Scheduled(cron = "${app.email-verification.cleanup-cron:0 45 3 * * *}")
    @Transactional
    public void cleanupExpired() {
        int deleted = tokenRepository.deleteExpired(Instant.now());
        if (deleted > 0) {
            log.info("Email verification cleanup: đã xoá {} token hết hạn", deleted);
        }
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}
