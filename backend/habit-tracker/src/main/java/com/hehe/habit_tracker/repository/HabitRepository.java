package com.hehe.habit_tracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hehe.habit_tracker.entity.Habit;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {

    @EntityGraph(attributePaths = { "iconHabit" })
    List<Habit> findByUserId(Long userId);

    @Override
    @EntityGraph(attributePaths = { "iconHabit" })
    Optional<Habit> findById(Long id);

    /** Habit có giờ nhắc + đang chạy — cho ReminderScheduler. JOIN FETCH user để lấy timezone. */
    @Query("SELECT h FROM Habit h JOIN FETCH h.user WHERE h.remindTime IS NOT NULL AND h.isPaused = false")
    List<Habit> findRemindable();
}
