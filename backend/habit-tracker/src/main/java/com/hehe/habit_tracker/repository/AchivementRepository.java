package com.hehe.habit_tracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hehe.habit_tracker.entity.Achivement;

@Repository
public interface AchivementRepository extends JpaRepository<Achivement, Long> {

    Optional<Achivement> findByCode(String code);

    boolean existsByCode(String code);

    List<Achivement> findAllByOrderBySortOrderAsc();

    /** Dòng có sortOrder lớn nhất — để service tính vị trí kế tiếp (max + 10). */
    Optional<Achivement> findFirstByOrderBySortOrderDesc();
}
