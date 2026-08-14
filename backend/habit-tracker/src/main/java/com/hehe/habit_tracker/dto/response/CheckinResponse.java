package com.hehe.habit_tracker.dto.response;

import java.time.Instant;
import java.time.LocalDate;

import lombok.Builder;

@Builder
public record CheckinResponse(
        Long id,
        Long habitId,
        LocalDate checkinDate,
        String note,
        Instant createdAt
) {
}
