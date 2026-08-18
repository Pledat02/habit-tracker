package com.hehe.habit_tracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(500, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(400, "Invalid message key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(400, "User already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(404, "User not found", HttpStatus.NOT_FOUND),
    HABIT_NOT_FOUND(404, "Habit not found", HttpStatus.NOT_FOUND),
    ICON_NOT_FOUND(404, "Icon not found", HttpStatus.NOT_FOUND),
    CHECKIN_NOT_FOUND(404, "Checkin not found", HttpStatus.NOT_FOUND),
    CHECKIN_EXISTED(400, "Already checked in on this date", HttpStatus.BAD_REQUEST),
    ACHIEVEMENT_NOT_FOUND(404, "Achievement not found", HttpStatus.NOT_FOUND),
    ACHIEVEMENT_CODE_EXISTED(400, "Achievement code already exists", HttpStatus.BAD_REQUEST),
    USER_ACHIEVEMENT_NOT_FOUND(404, "User achievement not found", HttpStatus.NOT_FOUND),
    USER_ACHIEVEMENT_EXISTED(400, "Achievement already unlocked", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(401, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_INVALID(401, "Invalid or expired refresh token", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(403, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_REQUEST(400, "Invalid request data", HttpStatus.BAD_REQUEST),
    RATE_LIMITED(429, "Too many requests, please try again later", HttpStatus.TOO_MANY_REQUESTS),
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
