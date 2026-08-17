package com.hehe.habit_tracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hehe.habit_tracker.entity.Habit;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {

    @EntityGraph(attributePaths = { "iconHabit" })
    List<Habit> findByUserId(Long userId);

    @Override
    @EntityGraph(attributePaths = { "iconHabit" })
    Optional<Habit> findById(Long id);
}
