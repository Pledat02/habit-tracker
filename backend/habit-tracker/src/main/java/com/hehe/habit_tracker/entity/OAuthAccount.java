package com.hehe.habit_tracker.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.hehe.habit_tracker.common.AuthProvider;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Liên kết 1 Users với 1 tài khoản ở nhà cung cấp OAuth bên ngoài (Google...).
 * Tách riêng khỏi Users để Users.password vẫn giữ đúng ngữ nghĩa "mật khẩu local"
 * và 1 user có thể liên kết nhiều provider trong tương lai (Facebook, GitHub...).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "oauth_accounts",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_oauth_provider_user",
        columnNames = {"provider", "provider_user_id"}
    )
)
public class OAuthAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AuthProvider provider;

    /** ID bất biến của Google cho user này (claim 'sub') — KHÔNG dùng email vì email có thể đổi. */
    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    /** Lưu tham khảo, không dùng để định danh (email có thể đổi phía Google). */
    private String email;

    @CreationTimestamp
    private Instant createdAt;
}
