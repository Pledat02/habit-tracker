package com.hehe.habit_tracker.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hehe.habit_tracker.common.AchievementCategory;
import com.hehe.habit_tracker.common.AchievementType;
import com.hehe.habit_tracker.entity.Achivement;
import com.hehe.habit_tracker.repository.AchivementRepository;

/**
 * Seed các định nghĩa thành tựu mặc định. Idempotent THEO CODE: chỉ thêm định nghĩa
 * còn thiếu, không đụng cái đã có. Không dùng count()>0 (kiểu đó khiến định nghĩa
 * mới thêm sau này không bao giờ được seed nếu bảng đã có dữ liệu).
 * Copy/icon khớp frontend/src/lib/achievements.ts để nhất quán 2 phía.
 */
@Configuration
public class AchievementSeeder {

    @Bean
    CommandLineRunner seedAchievementDefinitions(AchivementRepository achivementRepository) {
        return args -> {
            saveIfMissing(achivementRepository, streak("STREAK_7", "7 ngày", "Khởi đầu rực lửa!", 7, "Flame"));
            saveIfMissing(achivementRepository, streak("STREAK_30", "30 ngày", "Một tháng bền bỉ!", 30, "Flame"));
            saveIfMissing(achivementRepository, streak("STREAK_100", "100 ngày", "Bậc thầy kiên trì!", 100, "Medal"));
            saveIfMissing(achivementRepository, streak("STREAK_365", "365 ngày", "Huyền thoại một năm!", 365, "Trophy"));

            // Account-level: nhiều habit cùng đạt streak. target = số habit, target2 = ngày streak tối thiểu.
            saveIfMissing(achivementRepository, multiStreak("MULTI_STREAK_3_7", "Đa nhiệm", "3 habit cùng streak 7 ngày!", 3, 7));
            saveIfMissing(achivementRepository, multiStreak("MULTI_STREAK_5_30", "Bậc thầy đa nhiệm", "5 habit cùng streak 30 ngày!", 5, 30));
        };
    }

    private void saveIfMissing(AchivementRepository repo, Achivement def) {
        if (!repo.existsByCode(def.getCode())) {
            repo.save(def);
        }
    }

    private Achivement streak(String code, String name, String description, int target, String icon) {
        return Achivement.builder()
                .code(code)
                .category(AchievementCategory.PER_HABIT)
                .type(AchievementType.STREAK)
                .name(name)
                .description(description)
                .icon(icon)
                .target(target)
                .sortOrder(target)
                .active(true)
                .build();
    }

    private Achivement multiStreak(String code, String name, String description, int habits, int minDays) {
        return Achivement.builder()
                .code(code)
                .category(AchievementCategory.ACCOUNT)
                .type(AchievementType.MULTI_STREAK)
                .name(name)
                .description(description)
                .icon("Users")
                .target(habits)   // số habit
                .target2(minDays) // ngày streak tối thiểu mỗi habit
                .sortOrder(1000 + habits)
                .active(true)
                .build();
    }
}
