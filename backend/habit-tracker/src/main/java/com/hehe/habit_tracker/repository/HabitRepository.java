package com.hehe.habit_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hehe.habit_tracker.entity.Habit;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {
}
