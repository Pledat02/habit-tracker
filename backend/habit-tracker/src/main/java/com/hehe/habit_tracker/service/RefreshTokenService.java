package com.hehe.habit_tracker.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hehe.habit_tracker.entity.RefreshToken;
import com.hehe.habit_tracker.entity.Users;
import com.hehe.habit_tracker.exception.AppException;
import com.hehe.habit_tracker.exception.ErrorCode;
import com.hehe.habit_tracker.repository.RefreshTokenRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

/**
 * Quản lý vòng đời refresh token: phát hành, xoay vòng (rotate), thu hồi.
 *
 * Token GỐC (raw) chỉ tồn tại 1 lần lúc trả về cho client (để đặt vào cookie) —
 * KHÔNG BAO GIỜ lưu lại. DB chỉ giữ SHA-256 hash của nó, giống việc không ai lưu
 * mật khẩu dạng plaintext. Khác BCrypt (dùng cho password): token này là chuỗi
 * ngẫu nhiên 256-bit, entropy đã rất cao nên không cần hàm băm cố tình làm chậm —
 * SHA-256 (nhanh) là đủ và tránh tốn CPU không cần thiết mỗi lần refresh.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefreshTokenService {

    RefreshTokenRepository refreshTokenRepository;
    SecureRandom secureRandom = new SecureRandom();

    @Value("${jwt.refreshValidDuration}")
    @NonFinal
    long refreshValidDuration;

    /** Phát hành refresh token mới cho user (đăng nhập lần đầu). Trả về token GỐC để đặt vào cookie. */
    public String issue(Users user) {
        String rawToken = generateRawToken();

        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .expiresAt(Instant.now().plusSeconds(refreshValidDuration))
                .build();
        refreshTokenRepository.save(entity);

        return rawToken;
    }

    /**
     * Đổi refresh token cũ lấy refresh token mới (rotate) — gọi mỗi lần client xin access token mới
     * (mỗi F5 đều gọi, vì access token nằm trong RAM và mất khi tải lại trang).
     *
     * Rotate TẠI CHỖ: ghi đè hash + gia hạn trên CHÍNH dòng đang có, KHÔNG tạo dòng mới.
     * -> mỗi phiên (thiết bị) chỉ tồn tại 1 dòng, F5 bao nhiêu lần cũng không phình bảng.
     * Token cũ chết ngay vì hash của nó bị ghi đè khỏi DB (dùng lại -> không tìm thấy -> INVALID).
     *
     * Đánh đổi so với "insert dòng mới + revoke dòng cũ": mất khả năng phát hiện replay để
     * "thu hồi toàn bộ thiết bị" (vì hash cũ không còn để đối chiếu). Bù lại bảng không phình
     * theo số lần refresh — phù hợp với app này (token ở RAM nên refresh rất thường xuyên).
     */
    public RotateResult rotate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_INVALID));

        if (existing.isRevoked() || existing.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        String newRawToken = generateRawToken();
        existing.setTokenHash(hash(newRawToken));
        existing.setExpiresAt(Instant.now().plusSeconds(refreshValidDuration));
        refreshTokenRepository.save(existing); // UPDATE cùng dòng, không INSERT

        return new RotateResult(existing.getUser(), newRawToken);
    }

    /** Logout: XOÁ hẳn dòng refresh token (không để lại dòng revoked). Im lặng nếu không tìm thấy. */
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(hash(rawToken))
                .ifPresent(refreshTokenRepository::delete);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32]; // 256 bit
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

    public record RotateResult(Users user, String rawToken) {
    }
}
