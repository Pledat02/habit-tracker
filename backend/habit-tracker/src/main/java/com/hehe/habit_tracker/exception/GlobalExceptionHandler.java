package com.hehe.habit_tracker.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hehe.habit_tracker.common.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Trả về ResponseEntity (không phải ApiResponse trần) để set ĐÚNG HTTP status
    // theo errorCode.getStatusCode() — trả POJO trực tiếp sẽ luôn là HTTP 200
    // bất kể lỗi logic là gì, khiến client không thể phân biệt thành công/thất bại
    // qua status code (chỉ có wrapped JSON, không đáng tin cho fetch API/CORS/cache).
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<Void>> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handlingValidation(MethodArgumentNotValidException exception) {
        String defaultMessage = exception.getFieldError() != null
                ? exception.getFieldError().getDefaultMessage()
                : "Validation failed";
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatusCode())
                .body(ApiResponse.error(ErrorCode.INVALID_REQUEST.getCode(), defaultMessage));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<Void>> handlingException(Exception exception) {
        String message = (exception.getMessage() != null && !exception.getMessage().isBlank())
                ? exception.getMessage()
                : ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage();
        return ResponseEntity.status(ErrorCode.UNCATEGORIZED_EXCEPTION.getStatusCode())
                .body(ApiResponse.error(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode(), message));
    }
}
