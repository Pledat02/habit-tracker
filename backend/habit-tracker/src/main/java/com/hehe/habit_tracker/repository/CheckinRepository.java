package com.hehe.habit_tracker.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hehe.habit_tracker.entity.Checkin;

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    List<Checkin> findByHabitId(Long habitId);

    /** Tất cả check-in thuộc các habit của 1 user — điều hướng qua checkin.habit.user.id. */
    List<Checkin> findByHabitUserId(Long userId);

    /**
     * Check-in của 1 user trong khoảng ngày [from, to] (bao gồm 2 đầu). Dùng cho /checkins/me
     * có lọc thời gian: chặn payload theo thời gian thay vì trả toàn bộ lịch sử. Tận dụng
     * unique index (habit_id, checkin_date) sẵn có để range-scan theo ngày trong từng habit.
     */
    List<Checkin> findByHabitUserIdAndCheckinDateBetween(Long userId, LocalDate from, LocalDate to);

    boolean existsByHabitIdAndCheckinDate(Long habitId, LocalDate checkinDate);
}
