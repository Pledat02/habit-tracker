package com.hehe.habit_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hehe.habit_tracker.entity.IconHabit;

@Repository
public interface IconHabitRepository extends JpaRepository<IconHabit, Long> {
}
