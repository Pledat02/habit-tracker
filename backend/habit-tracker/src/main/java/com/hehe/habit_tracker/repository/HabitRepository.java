package com.hehe.habit_tracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hehe.habit_tracker.entity.Habit;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {

    List<Habit> findByUserId(Long userId);
}
