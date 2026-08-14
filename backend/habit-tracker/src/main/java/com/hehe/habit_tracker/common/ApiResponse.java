package com.hehe.habit_tracker.common;

public record ApiResponse<T>(
        T data,
        String message,
        int status) {

    public static <T> ApiResponse<T> success(T data, int status) {
        return new ApiResponse<>(data, "success", status);
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(null, message, status);
    }
}
