package com.hehe.habit_tracker.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hehe.habit_tracker.entity.Checkin;

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    List<Checkin> findByHabitId(Long habitId);

    boolean existsByHabitIdAndCheckinDate(Long habitId, LocalDate checkinDate);
}
