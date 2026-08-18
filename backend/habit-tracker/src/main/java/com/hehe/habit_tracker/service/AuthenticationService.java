package com.hehe.habit_tracker.service;

import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hehe.habit_tracker.config.RefreshCookieUtil;
import com.hehe.habit_tracker.dto.request.AuthenticationRequest;
import com.hehe.habit_tracker.dto.response.AuthenticationResponse;
import com.hehe.habit_tracker.entity.Users;
import com.hehe.habit_tracker.exception.AppException;
import com.hehe.habit_tracker.exception.ErrorCode;
import com.hehe.habit_tracker.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    TokenService tokenService;
    RefreshTokenService refreshTokenService;
    RefreshCookieUtil refreshCookieUtil;

    /**
     * Đăng nhập bằng password: trả access token, ĐẶT refresh token vào cookie HttpOnly.
     * `request.username()` chấp nhận CẢ username lẫn email — frontend hiện thu email
     * ở form đăng nhập, nên tra username trước, không thấy thì thử theo email.
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletResponse response) {
        Users user = userRepository.findByUsername(request.username())
                .or(() -> userRepository.findByEmail(request.username()))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Tài khoản chỉ đăng nhập qua OAuth (vd Google) không có password local.
        if (user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return issueTokens(user, response);
    }

    /**
     * Đổi refresh token (từ cookie) lấy access token mới, đồng thời XOAY VÒNG refresh token
     * (token cũ vô hiệu, cookie mới thay thế). Gọi khi access token hết hạn.
     */
    public AuthenticationResponse refresh(String rawRefreshToken, HttpServletResponse response) {
        RefreshTokenService.RotateResult result = refreshTokenService.rotate(rawRefreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookieUtil.build(result.rawToken()));
        return AuthenticationResponse.builder()
                .token(tokenService.generateToken(result.user()))
                .authenticated(true)
                .build();
    }

    /** Logout: thu hồi refresh token hiện tại + xoá cookie. Access token cũ vẫn tự hết hạn theo thời gian sống. */
    public void logout(String rawRefreshToken, HttpServletResponse response) {
        refreshTokenService.revoke(rawRefreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookieUtil.clear());
    }

    /** Dùng chung cho login thường và login Google: phát access token + đặt cookie refresh token. */
    public AuthenticationResponse issueTokens(Users user, HttpServletResponse response) {
        String accessToken = tokenService.generateToken(user);
        String rawRefreshToken = refreshTokenService.issue(user);

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookieUtil.build(rawRefreshToken));
        return AuthenticationResponse.builder()
                .token(accessToken)
                .authenticated(true)
                .build();
    }
}
