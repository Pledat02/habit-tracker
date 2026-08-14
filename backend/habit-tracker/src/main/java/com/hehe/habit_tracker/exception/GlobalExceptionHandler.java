package com.hehe.habit_tracker.exception;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hehe.habit_tracker.common.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = AppException.class)
    public ApiResponse<Void> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ApiResponse.error(errorCode.getCode(), errorCode.getMessage());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ApiResponse<Void> handlingValidation(MethodArgumentNotValidException exception) {
        String defaultMessage = exception.getFieldError() != null
                ? exception.getFieldError().getDefaultMessage()
                : "Validation failed";
        return ApiResponse.error(ErrorCode.INVALID_REQUEST.getCode(), defaultMessage);
    }

    @ExceptionHandler(value = Exception.class)
    public ApiResponse<Void> handlingException(Exception exception) {
        String message = (exception.getMessage() != null && !exception.getMessage().isBlank())
                ? exception.getMessage()
                : ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage();
        return ApiResponse.error(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode(), message);
    }
}
