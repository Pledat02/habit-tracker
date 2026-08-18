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

import com.hehe.habit_tracker.entity.Checkin;
import com.hehe.habit_tracker.entity.Habit;
import com.hehe.habit_tracker.entity.Users;
import com.hehe.habit_tracker.exception.AppException;
import com.hehe.habit_tracker.exception.ErrorCode;
import com.hehe.habit_tracker.mapper.CheckinMapper;
import com.hehe.habit_tracker.repository.CheckinRepository;
import com.hehe.habit_tracker.repository.HabitRepository;

/**
 * IDOR: checkin không có field user -> chủ suy qua checkin.habit.user.
 * Thao tác check-in/habit của người khác -> 404 (HABIT_NOT_FOUND hoặc CHECKIN_NOT_FOUND).
 */
class CheckinServiceOwnershipTest {

    private final CheckinRepository checkinRepository = mock(CheckinRepository.class);
    private final HabitRepository habitRepository = mock(HabitRepository.class);
    private final CheckinMapper checkinMapper = mock(CheckinMapper.class);
    private final AchievementEngine achievementEngine = mock(AchievementEngine.class);

    private final CheckinService service =
            new CheckinService(checkinRepository, habitRepository, checkinMapper, achievementEngine);

    private static final long OWNER = 1L;
    private static final long ATTACKER = 999L;

    private Habit habitOwnedByOwner() {
        Users owner = new Users();
        owner.setId(OWNER);
        Habit h = new Habit();
        h.setId(100L);
        h.setUser(owner);
        return h;
    }

    @Test
    void getCheckinsByHabit_otherUsersHabit_throwsHabitNotFound() {
        when(habitRepository.findById(100L)).thenReturn(Optional.of(habitOwnedByOwner()));

        AppException ex = assertThrows(AppException.class, () -> service.getCheckinsByHabit(100L, ATTACKER));
        assertEquals(ErrorCode.HABIT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getCheckinById_otherUser_throwsCheckinNotFound() {
        Checkin checkin = Checkin.builder().habit(habitOwnedByOwner()).build();
        checkin.setId(50L);
        when(checkinRepository.findById(50L)).thenReturn(Optional.of(checkin));

        AppException ex = assertThrows(AppException.class, () -> service.getCheckinById(50L, ATTACKER));
        assertEquals(ErrorCode.CHECKIN_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void deleteCheckin_otherUser_throwsAndDoesNotDelete() {
        Checkin checkin = Checkin.builder().habit(habitOwnedByOwner()).build();
        checkin.setId(50L);
        when(checkinRepository.findById(50L)).thenReturn(Optional.of(checkin));

        assertThrows(AppException.class, () -> service.deleteCheckin(50L, ATTACKER));
        verify(checkinRepository, never()).delete(any());
    }
}
