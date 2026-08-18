package com.hehe.habit_tracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hehe.habit_tracker.entity.UserAchivement;

@Repository
public interface UserAchivementRepository extends JpaRepository<UserAchivement, Long> {

    /**
     * Query xương sống: mọi thành tựu đã mở khoá của 1 user (cả per-habit lẫn
     * account).
     */
    @EntityGraph(attributePaths = { "definition" })
    List<UserAchivement> findByUserId(Long userId);

    /** Dedup thành tựu per-habit. */
    boolean existsByUserIdAndDefinitionIdAndHabitId(Long userId, Long definitionId, Long habitId);

    /**
     * Dedup thành tựu account-level (habit_id NULL) — bù cho unique constraint
     * không bắt NULL.
     */
    boolean existsByUserIdAndDefinitionIdAndHabitIsNull(Long userId, Long definitionId);
}
