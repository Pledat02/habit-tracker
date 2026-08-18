package com.hehe.habit_tracker.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hehe.habit_tracker.entity.PasswordResetToken;
import com.hehe.habit_tracker.entity.Users;
import com.hehe.habit_tracker.exception.AppException;
import com.hehe.habit_tracker.exception.ErrorCode;
import com.hehe.habit_tracker.repository.PasswordResetTokenRepository;
import com.hehe.habit_tracker.repository.RefreshTokenRepository;
import com.hehe.habit_tracker.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

/**
 * Đặt lại mật khẩu qua email. Nguyên tắc bảo mật:
 *  - DB chỉ lưu HASH của token (giống refresh token); token gốc chỉ ở trong link email.
 *  - {@link #requestReset} LUÔN trả như nhau dù email tồn tại hay không -> không lộ email nào đã đăng ký.
 *  - Token dùng 1 lần, hạn ngắn; reset xong vô hiệu mọi token reset còn lại của user.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PasswordResetService {

    UserRepository userRepository;
    PasswordResetTokenRepository resetTokenRepository;
    RefreshTokenRepository refreshTokenRepository;
    PasswordEncoder passwordEncoder;
    EmailService emailService;
    SecureRandom secureRandom = new SecureRandom();

    @Value("${app.password-reset.token-ttl-seconds:1800}") // 30 phút
    @NonFinal
    long ttlSeconds;

    @Value("${app.password-reset.link-base:${app.frontend-origin}/reset-password}")
    @NonFinal
    String linkBase;

    /** Sinh token, lưu hash, gửi link qua email. Luôn "thành công" về phía client (chống dò email). */
    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            // Vô hiệu token cũ còn sống -> mỗi lần xin chỉ 1 link hợp lệ.
            resetTokenRepository.markAllUsedForUser(user.getId());

            String rawToken = randomToken();
            PasswordResetToken token = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(hash(rawToken))
                    .expiresAt(Instant.now().plusSeconds(ttlSeconds))
                    .used(false)
                    .build();
            resetTokenRepository.save(token);

            String link = linkBase + "?token=" + rawToken;
            emailService.send(user.getEmail(), "Đặt lại mật khẩu Habit Tracker",
                    "Bấm vào link sau để đặt lại mật khẩu (hết hạn sau "
                            + (ttlSeconds / 60) + " phút):\n\n" + link
                            + "\n\nNếu bạn không yêu cầu, hãy bỏ qua email này.");
        }, () -> log.info("Yêu cầu reset cho email không tồn tại (bỏ qua âm thầm): {}", email));
    }

    /** Đổi mật khẩu bằng token. Token sai/hết hạn/đã dùng -> RESET_TOKEN_INVALID. */
    @Transactional
    public void confirmReset(String rawToken, String newPassword) {
        PasswordResetToken token = resetTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new AppException(ErrorCode.RESET_TOKEN_INVALID));
        if (token.isUsed() || token.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.RESET_TOKEN_INVALID);
        }

        Users user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        resetTokenRepository.save(token);
        // Đổi mật khẩu -> thu hồi mọi phiên đăng nhập cũ (buộc đăng nhập lại ở mọi thiết bị).
        refreshTokenRepository.revokeAllByUserId(user.getId());
    }

    @Scheduled(cron = "${app.password-reset.cleanup-cron:0 30 3 * * *}")
    @Transactional
    public void cleanupExpired() {
        int deleted = resetTokenRepository.deleteExpired(Instant.now());
        if (deleted > 0) {
            log.info("Password reset cleanup: đã xoá {} token hết hạn", deleted);
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
