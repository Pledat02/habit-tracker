package com.hehe.habit_tracker.dto.request;

import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HabitCreationRequest(

        @NotBlank(message = "Habit name is required") @Size(min = 3, max = 100, message = "Habit name must be between 3 and 100 characters") String name,
        @NotBlank(message = "Frequency is required") String frequency,
        String note,
        LocalTime remindTime

) {
}