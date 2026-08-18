package com.hehe.habit_tracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.hehe.habit_tracker.dto.request.UserAchivementCreationRequest;
import com.hehe.habit_tracker.entity.Achivement;
import com.hehe.habit_tracker.entity.Habit;
import com.hehe.habit_tracker.entity.UserAchivement;
import com.hehe.habit_tracker.entity.Users;
import com.hehe.habit_tracker.exception.AppException;
import com.hehe.habit_tracker.exception.ErrorCode;
import com.hehe.habit_tracker.mapper.UserAchivementMapper;
import com.hehe.habit_tracker.repository.AchivementRepository;
import com.hehe.habit_tracker.repository.HabitRepository;
import com.hehe.habit_tracker.repository.UserAchivementRepository;
import com.hehe.habit_tracker.repository.UserRepository;

/** IDOR cho thành tựu: chỉ xem được thành tựu của mình, không gắn thành tựu vào habit người khác. */
class UserAchivementServiceOwnershipTest {

    private final UserAchivementRepository userAchivementRepository = mock(UserAchivementRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final AchivementRepository achivementRepository = mock(AchivementRepository.class);
    private final HabitRepository habitRepository = mock(HabitRepository.class);
    private final UserAchivementMapper mapper = mock(UserAchivementMapper.class);

    private final UserAchivementService service = new UserAchivementService(
            userAchivementRepository, userRepository, achivementRepository, habitRepository, mapper);

    private static final long OWNER = 1L;
    private static final long ATTACKER = 999L;

    private Users owner() {
        Users u = new Users();
        u.setId(OWNER);
        return u;
    }

    @Test
    void getById_otherUsersRecord_throwsNotFound() {
        UserAchivement ua = UserAchivement.builder().user(owner()).build();
        ua.setId(7L);
        when(userAchivementRepository.findById(7L)).thenReturn(Optional.of(ua));

        AppException ex = assertThrows(AppException.class, () -> service.getById(7L, ATTACKER));
        assertEquals(ErrorCode.USER_ACHIEVEMENT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void grant_withOtherUsersHabit_throwsHabitNotFound_andDoesNotSave() {
        Achivement def = Achivement.builder().code("STREAK_7").build();
        def.setId(3L);
        when(achivementRepository.findById(3L)).thenReturn(Optional.of(def));

        Habit othersHabit = new Habit();
        othersHabit.setId(100L);
        othersHabit.setUser(owner()); // thuộc OWNER
        when(habitRepository.findById(100L)).thenReturn(Optional.of(othersHabit));

        // ATTACKER cố cấp thành tựu gắn vào habit của OWNER.
        UserAchivementCreationRequest req = new UserAchivementCreationRequest(3L, 100L);

        AppException ex = assertThrows(AppException.class, () -> service.grant(req, ATTACKER));
        assertEquals(ErrorCode.HABIT_NOT_FOUND, ex.getErrorCode());
        verify(userAchivementRepository, never()).save(any());
    }
}
