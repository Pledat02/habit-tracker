package com.hehe.habit_tracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.hehe.habit_tracker.common.AchievementCategory;
import com.hehe.habit_tracker.common.AchievementType;
import com.hehe.habit_tracker.dto.response.UserAchivementResponse;
import com.hehe.habit_tracker.entity.Achivement;
import com.hehe.habit_tracker.entity.Checkin;
import com.hehe.habit_tracker.entity.Habit;
import com.hehe.habit_tracker.entity.UserAchivement;
import com.hehe.habit_tracker.entity.Users;
import com.hehe.habit_tracker.mapper.UserAchivementMapper;
import com.hehe.habit_tracker.repository.AchivementRepository;
import com.hehe.habit_tracker.repository.CheckinRepository;
import com.hehe.habit_tracker.repository.HabitRepository;
import com.hehe.habit_tracker.repository.UserAchivementRepository;

/**
 * Unit test cho AchievementEngine — logic đánh giá & cấp thành tựu. Mock repository/mapper,
 * dùng StreakCalculator THẬT (để test cả engine lẫn tính streak). Không cần Docker.
 */
class AchievementEngineTest {

    private final HabitRepository habitRepository = mock(HabitRepository.class);
    private final CheckinRepository checkinRepository = mock(CheckinRepository.class);
    private final AchivementRepository achivementRepository = mock(AchivementRepository.class);
    private final UserAchivementRepository userAchivementRepository = mock(UserAchivementRepository.class);
    private final UserAchivementMapper mapper = mock(UserAchivementMapper.class);

    private AchievementEngine engine;
    private Users user;

    @BeforeEach
    void setup() {
        engine = new AchievementEngine(habitRepository, checkinRepository, achivementRepository,
                userAchivementRepository, mapper, new StreakCalculator());
        ReflectionTestUtils.setField(engine, "defaultTimezone", "UTC");

        user = new Users();
        user.setId(1L);
        user.setZoneId("UTC"); // "hôm nay" tính theo UTC cho khớp mốc test

        // mapper trả response mang code của definition để assert.
        when(mapper.toUserAchivementResponse(any())).thenAnswer(inv -> {
            UserAchivement ua = inv.getArgument(0);
            return UserAchivementResponse.builder().code(ua.getDefinition().getCode()).build();
        });
        when(userAchivementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // Mặc định chưa unlock gì (các test cần khác thì override).
        when(userAchivementRepository.existsByUserIdAndDefinitionIdAndHabitId(anyLong(), anyLong(), anyLong()))
                .thenReturn(false);
        when(userAchivementRepository.existsByUserIdAndDefinitionIdAndHabitIsNull(anyLong(), anyLong()))
                .thenReturn(false);
    }

    private LocalDate today() {
        return LocalDate.now(ZoneId.of("UTC"));
    }

    private Habit habit(long id) {
        Habit h = new Habit();
        h.setId(id);
        h.setName("H" + id);
        h.setFrequency("\"daily\"");
        h.setUser(user);
        return h;
    }

    private Achivement streakDef(long id, String code, int target) {
        Achivement a = Achivement.builder()
                .code(code).category(AchievementCategory.PER_HABIT).type(AchievementType.STREAK)
                .name(code).target(target).build();
        a.setId(id);
        return a;
    }

    private List<Checkin> consecutiveCheckins(Habit h, int days) {
        List<Checkin> list = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            list.add(Checkin.builder().habit(h).checkinDate(today().minusDays(i)).build());
        }
        return list;
    }

    @Test
    void grantsStreak7_whenHabitReaches7DayStreak() {
        Habit h = habit(10L);
        when(checkinRepository.findByHabitUserId(1L)).thenReturn(consecutiveCheckins(h, 7));
        when(habitRepository.findByUserId(1L)).thenReturn(List.of(h));
        when(achivementRepository.findByTypeAndActiveTrue(AchievementType.STREAK))
                .thenReturn(List.of(streakDef(1L, "STREAK_7", 7), streakDef(2L, "STREAK_30", 30)));
        when(achivementRepository.findByTypeAndActiveTrue(AchievementType.MULTI_STREAK)).thenReturn(List.of());

        List<UserAchivementResponse> result = engine.evaluate(user, h);

        assertEquals(1, result.size(), "chỉ STREAK_7 đạt ngưỡng, STREAK_30 chưa");
        assertEquals("STREAK_7", result.get(0).code());
    }

    @Test
    void doesNotGrant_whenStreakBelowThreshold() {
        Habit h = habit(10L);
        when(checkinRepository.findByHabitUserId(1L)).thenReturn(consecutiveCheckins(h, 2)); // streak 2 < 7
        when(habitRepository.findByUserId(1L)).thenReturn(List.of(h));
        when(achivementRepository.findByTypeAndActiveTrue(AchievementType.STREAK))
                .thenReturn(List.of(streakDef(1L, "STREAK_7", 7)));
        when(achivementRepository.findByTypeAndActiveTrue(AchievementType.MULTI_STREAK)).thenReturn(List.of());

        assertTrue(engine.evaluate(user, h).isEmpty());
    }

    @Test
    void doesNotRegrant_whenAlreadyUnlocked() {
        Habit h = habit(10L);
        when(checkinRepository.findByHabitUserId(1L)).thenReturn(consecutiveCheckins(h, 7));
        when(habitRepository.findByUserId(1L)).thenReturn(List.of(h));
        when(achivementRepository.findByTypeAndActiveTrue(AchievementType.STREAK))
                .thenReturn(List.of(streakDef(1L, "STREAK_7", 7)));
        when(achivementRepository.findByTypeAndActiveTrue(AchievementType.MULTI_STREAK)).thenReturn(List.of());
        // Đã unlock STREAK_7 (def id 1) cho habit id 10 -> không cấp lại.
        when(userAchivementRepository.existsByUserIdAndDefinitionIdAndHabitId(1L, 1L, 10L)).thenReturn(true);

        assertTrue(engine.evaluate(user, h).isEmpty());
    }

    @Test
    void grantsMultiStreak_when3HabitsReach7DayStreak() {
        Habit h1 = habit(10L);
        Habit h2 = habit(11L);
        Habit h3 = habit(12L);
        List<Checkin> all = new ArrayList<>();
        all.addAll(consecutiveCheckins(h1, 7));
        all.addAll(consecutiveCheckins(h2, 7));
        all.addAll(consecutiveCheckins(h3, 7));
        when(checkinRepository.findByHabitUserId(1L)).thenReturn(all);
        when(habitRepository.findByUserId(1L)).thenReturn(List.of(h1, h2, h3));
        when(achivementRepository.findByTypeAndActiveTrue(AchievementType.STREAK)).thenReturn(List.of());

        Achivement multi = Achivement.builder()
                .code("MULTI_STREAK_3_7").category(AchievementCategory.ACCOUNT)
                .type(AchievementType.MULTI_STREAK).name("multi").target(3).target2(7).build();
        multi.setId(5L);
        when(achivementRepository.findByTypeAndActiveTrue(AchievementType.MULTI_STREAK)).thenReturn(List.of(multi));

        List<UserAchivementResponse> result = engine.evaluate(user, h3);

        assertEquals(1, result.size());
        assertEquals("MULTI_STREAK_3_7", result.get(0).code());
    }

    @Test
    void multiStreak_notGranted_whenOnly2HabitsQualify() {
        Habit h1 = habit(10L);
        Habit h2 = habit(11L);
        Habit h3 = habit(12L);
        List<Checkin> all = new ArrayList<>();
        all.addAll(consecutiveCheckins(h1, 7));
        all.addAll(consecutiveCheckins(h2, 7));
        all.addAll(consecutiveCheckins(h3, 3)); // habit 3 chỉ streak 3 -> chỉ 2 habit đạt >=7
        when(checkinRepository.findByHabitUserId(1L)).thenReturn(all);
        when(habitRepository.findByUserId(1L)).thenReturn(List.of(h1, h2, h3));
        when(achivementRepository.findByTypeAndActiveTrue(AchievementType.STREAK)).thenReturn(List.of());

        Achivement multi = Achivement.builder()
                .code("MULTI_STREAK_3_7").category(AchievementCategory.ACCOUNT)
                .type(AchievementType.MULTI_STREAK).name("multi").target(3).target2(7).build();
        multi.setId(5L);
        when(achivementRepository.findByTypeAndActiveTrue(AchievementType.MULTI_STREAK)).thenReturn(List.of(multi));

        assertTrue(engine.evaluate(user, h1).isEmpty());
    }
}
