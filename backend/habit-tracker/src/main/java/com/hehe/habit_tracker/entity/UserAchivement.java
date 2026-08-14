package com.hehe.habit_tracker.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

/**
 * Bản ghi MỞ KHÓA: user nào đạt thành tựu nào, lúc nào.
 * habit = null  → thành tựu toàn tài khoản (ACCOUNT).
 * habit != null → thành tựu gắn với 1 habit cụ thể (PER_HABIT).
 */
@Data
@Entity
@Table(
    name = "user_achivements",
    // Chặn mở khóa trùng cho thành tựu per-habit (user + định nghĩa + habit).
    // Lưu ý: dòng ACCOUNT (habit_id NULL) KHÔNG được ràng buộc này dedupe
    // (NULL không đụng nhau) → cần partial unique index trong migration
    // hoặc kiểm tra ở service. Xem ghi chú kèm theo.
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_ach_habit",
        columnNames = {"user_id", "definition_id", "habit_id"}
    )
)
public class UserAchivement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Giữ user_id trên MỌI dòng để query "thành tựu của tôi" bằng 1 câu phẳng. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    /** Trỏ về catalog để lấy name/icon/type khi hiển thị. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "definition_id", nullable = false)
    private Achivement definition;

    /** NULL = thành tựu account-level. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_id")
    private Habit habit;

    @CreationTimestamp
    private Instant unlockedAt;

    private boolean shared = false;
}
