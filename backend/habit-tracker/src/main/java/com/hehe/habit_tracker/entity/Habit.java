package com.hehe.habit_tracker.entity;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

// Cùng lý do như Users: @Data một mình (không @NoArgsConstructor) chỉ sinh constructor
// theo các field @NonNull (name, frequency) -> Hibernate THIẾU constructor rỗng để
// hydrate entity từ DB, chỉ lộ ra lúc chạy thật (log HHH000182), không lỗi lúc compile.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "habits")
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull
    private String name;
    @NonNull
    private String frequency;
    private String note;
    private LocalTime remindTime;
    @Builder.Default
    private boolean isPaused = false;
    // Id event tương ứng trong Google Calendar (đồng bộ 1 chiều). Null = chưa đẩy lên (V9).
    private String googleCalendarEventId;
    // Streak KHÔNG lưu cột: tính on-the-fly bằng StreakCalculator (cột cũ best_streak/
    // current_streak/last_checkin_date đã bỏ ở migration V5, chúng chưa bao giờ được cập nhật).
    @OneToOne
    @JoinColumn(name = "icon_id")
    private IconHabit iconHabit;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;
    @OneToMany(mappedBy = "habit")
    private List<UserAchivement> achivements;
    @OneToMany(mappedBy = "habit")
    private List<Checkin> checkins;

    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;

}
