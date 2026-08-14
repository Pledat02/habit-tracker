package com.hehe.habit_tracker.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record CheckinCreationRequest(

        @NotNull(message = "habitId is required") Long habitId,
        /** Null => mặc định hôm nay (xử lý ở service). */
        LocalDate checkinDate,
        String note

) {
}
