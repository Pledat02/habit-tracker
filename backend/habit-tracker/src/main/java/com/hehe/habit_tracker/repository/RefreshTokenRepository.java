package com.hehe.habit_tracker.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hehe.habit_tracker.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Dùng khi phát hiện refresh token bị dùng lại sau khi đã rotate — coi như bị lộ, đăng xuất mọi thiết bị. */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user.id = :userId AND r.revoked = false")
    void revokeAllByUserId(Long userId);

    /**
     * Xoá token "chết": đã hết hạn (không dùng được nữa) HOẶC đã revoked quá thời gian
     * ân hạn (dòng do rotate/logout sinh ra — chỉ cần giữ ngắn để phát hiện token bị lộ).
     * Token đang hiệu lực (chưa hết hạn, chưa revoked) KHÔNG bị đụng.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now "
            + "OR (r.revoked = true AND r.createdAt < :revokedCutoff)")
    int deleteStale(@Param("now") Instant now, @Param("revokedCutoff") Instant revokedCutoff);
}
