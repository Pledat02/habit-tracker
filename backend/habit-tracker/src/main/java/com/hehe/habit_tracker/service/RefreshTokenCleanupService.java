package com.hehe.habit_tracker.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hehe.habit_tracker.repository.RefreshTokenRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

/**
 * Dọn định kỳ bảng refresh_tokens để nó không phình vô hạn. Cần thiết vì mỗi lần
 * login/rotate đều tạo dòng mới, và dòng cũ chỉ được đánh dấu revoked (không xoá).
 *
 * Chạy theo cron (mặc định 3h sáng mỗi ngày). Xoá:
 *  - token đã hết hạn: vô dụng, xoá được ngay.
 *  - token revoked quá thời gian ân hạn (mặc định 1 ngày): dòng do rotate/logout,
 *    chỉ giữ ngắn để còn phát hiện replay token bị đánh cắp; sau đó xoá.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefreshTokenCleanupService {

    RefreshTokenRepository refreshTokenRepository;

    /** Thời gian giữ token đã revoked (giây) trước khi xoá — cửa sổ phát hiện token bị lộ. */
    @Value("${app.refresh-token-revoked-grace-seconds:86400}")
    @NonFinal
    long revokedGraceSeconds;

    @Scheduled(cron = "${app.refresh-token-cleanup-cron:0 0 3 * * *}")
    @Transactional
    public void cleanupStaleTokens() {
        Instant now = Instant.now();
        int deleted = refreshTokenRepository.deleteStale(now, now.minusSeconds(revokedGraceSeconds));
        if (deleted > 0) {
            log.info("Refresh token cleanup: đã xoá {} token chết", deleted);
        }
    }
}
