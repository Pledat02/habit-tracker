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

import com.hehe.habit_tracker.dto.response.HabitResponse;
import com.hehe.habit_tracker.entity.Habit;
import com.hehe.habit_tracker.entity.Users;
import com.hehe.habit_tracker.exception.AppException;
import com.hehe.habit_tracker.exception.ErrorCode;
import com.hehe.habit_tracker.mapper.HabitMapper;
import com.hehe.habit_tracker.repository.HabitRepository;
import com.hehe.habit_tracker.repository.IconHabitRepository;
import com.hehe.habit_tracker.repository.UserRepository;

/** IDOR: user chỉ thao tác được habit CỦA MÌNH. Sai chủ / không tồn tại -> HABIT_NOT_FOUND (404). */
class HabitServiceOwnershipTest {

    private final HabitRepository habitRepository = mock(HabitRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final IconHabitRepository iconHabitRepository = mock(IconHabitRepository.class);
    private final HabitMapper habitMapper = mock(HabitMapper.class);

    private final HabitService service =
            new HabitService(habitRepository, userRepository, iconHabitRepository, habitMapper);

    private static final long OWNER = 1L;
    private static final long ATTACKER = 999L;
    private static final long HABIT_ID = 100L;

    private Habit habitOwnedByOwner() {
        Users owner = new Users();
        owner.setId(OWNER);
        Habit h = new Habit();
        h.setId(HABIT_ID);
        h.setUser(owner);
        return h;
    }

    @Test
    void getById_byOwner_ok() {
        when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habitOwnedByOwner()));
        when(habitMapper.toHabitResponse(any())).thenReturn(HabitResponse.builder().id(HABIT_ID).build());

        HabitResponse res = service.getHabitById(HABIT_ID, OWNER);
        assertEquals(HABIT_ID, res.id());
    }

    @Test
    void getById_byOtherUser_throwsNotFound() {
        when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habitOwnedByOwner()));

        AppException ex = assertThrows(AppException.class, () -> service.getHabitById(HABIT_ID, ATTACKER));
        assertEquals(ErrorCode.HABIT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void delete_byOtherUser_throwsAndDoesNotDelete() {
        when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habitOwnedByOwner()));

        assertThrows(AppException.class, () -> service.deleteHabit(HABIT_ID, ATTACKER));
        verify(habitRepository, never()).delete(any());
    }

    @Test
    void getById_nonExistent_throwsNotFound() {
        when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> service.getHabitById(HABIT_ID, OWNER));
        assertEquals(ErrorCode.HABIT_NOT_FOUND, ex.getErrorCode());
    }
}
