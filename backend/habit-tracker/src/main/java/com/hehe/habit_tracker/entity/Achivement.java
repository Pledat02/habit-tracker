package com.hehe.habit_tracker.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.hehe.habit_tracker.common.AchievementCategory;
import com.hehe.habit_tracker.common.AchievementType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Catalog (định nghĩa) của thành tựu — dữ liệu tĩnh, seed lúc khởi động (xem
 * config/AchievementSeeder.java). KHÔNG lưu "ai mở khóa": việc đó nằm ở {@link UserAchivement}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "achivements")
public class Achivement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Định danh máy, bất biến, duy nhất: 'STREAK_7', 'HOLIDAY_CHECKIN'... */
    @Column(nullable = false, unique = true, length = 64)
    private String code;

    /** Gắn với 1 habit hay cả tài khoản. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AchievementCategory category;

    /** Loại luật đánh giá → chọn handler ở service. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AchievementType type;

    /** Tên hiển thị (được phép đổi/dịch, khác với code). */
    @Column(nullable = false, length = 128)
    private String name;

    private String description;

    /** Lucide icon name: 'Flame', 'Trophy'... */
    private String icon;

    /** Ngưỡng chính. STREAK: số ngày. MULTI_STREAK: số habit cần đạt. */
    private Integer target;

    /** Ngưỡng phụ (chỉ vài loại). MULTI_STREAK: số ngày streak tối thiểu mỗi habit phải đạt. */
    private Integer target2;

    /** Thứ tự hiển thị trên UI (bước nhảy 10 để dễ chèn về sau). */
    @Column(nullable = false)
    @Builder.Default
    private int sortOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}
