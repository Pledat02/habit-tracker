package com.hehe.habit_tracker.entity;

import java.time.Instant;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
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

@Data
@Entity
@Table(
    name = "checkins",
    // Mỗi habit chỉ check-in 1 lần / ngày.
    uniqueConstraints = @UniqueConstraint(
        name = "uq_checkin_habit_date",
        columnNames = {"habit_id", "checkin_date"}
    )
)
public class Checkin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;

    /** Ngày check-in (theo ngày, không theo giờ). */
    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;

    private String note;

    @CreationTimestamp
    private Instant createdAt;
}
