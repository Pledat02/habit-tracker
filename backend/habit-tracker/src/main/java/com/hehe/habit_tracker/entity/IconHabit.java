package com.hehe.habit_tracker.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Trước đây cả Habit VÀ IconHabit cùng khai @JoinColumn cho quan hệ 1-1 này
// -> Hibernate tạo 2 cột FK (icon_id trên habits, habit_id trên icons) cho
// đúng 1 quan hệ. Habit là owning side (icon_id) — IconHabit chỉ mappedBy,
// không tự thêm cột.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "icons")
public class IconHabit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String icon;
    private String iconColor;
    private String iconRef;

    @OneToOne(mappedBy = "iconHabit")
    private Habit habit;
    private boolean isActived;
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}