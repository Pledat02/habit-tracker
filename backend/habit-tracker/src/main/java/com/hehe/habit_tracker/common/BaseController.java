package com.hehe.habit_tracker.common;

import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;

public abstract class BaseController<T> {

    /**
     * Lấy userId trực tiếp từ claim 'userId' của JWT đã verify — KHÔNG tra bảng users.
     * Token do TokenService phát ra luôn kèm claim này, nên các endpoint chỉ cần lọc/so
     * theo userId không phải nạp cả entity Users (tránh câu SELECT users thừa mỗi request).
     * Claim số được decode thành Long/Integer -> ép về long qua Number cho chắc.
     */
    protected Long currentUserId(Jwt jwt) {
        Number userId = jwt.getClaim("userId");
        return userId.longValue();
    }

    // for create
    protected ApiResponse<T> createSuccessResponse(T data) {
        return ApiResponse.success(data, 201);
    }

    // for read
    protected ApiResponse<T> readSuccessResponse(T data) {
        return ApiResponse.success(data, 200);
    }

    // for read list
    protected ApiResponse<List<T>> readListSuccessResponse(List<T> data) {
        return ApiResponse.success(data, 200);
    }

    // for update
    protected ApiResponse<T> updateSuccessResponse(T data) {
        return ApiResponse.success(data, 200);
    }

    // for delete
    protected ApiResponse<T> deleteSuccessResponse() {
        return ApiResponse.success(null, 204);
    }

}
