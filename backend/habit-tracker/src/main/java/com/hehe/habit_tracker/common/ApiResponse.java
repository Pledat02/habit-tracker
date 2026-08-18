package com.hehe.habit_tracker.common;

/**
 * Envelope thống nhất cho mọi response. {@code code} là mã MÁY-ĐỌC (tên ErrorCode, vd
 * "USER_EXISTED") để frontend switch theo mã thay vì dò chuỗi message — message có thể đổi
 * (đa ngôn ngữ) mà mã thì ổn định. Response thành công có code = "SUCCESS".
 */
public record ApiResponse<T>(
        T data,
        String message,
        String code,
        int status) {

    public static <T> ApiResponse<T> success(T data, int status) {
        return new ApiResponse<>(data, "success", "SUCCESS", status);
    }

    public static <T> ApiResponse<T> error(int status, String message, String code) {
        return new ApiResponse<>(null, message, code, status);
    }
}
