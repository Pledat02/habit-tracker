package com.hehe.habit_tracker.common;

import java.util.List;

public abstract class BaseController<T> {

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
