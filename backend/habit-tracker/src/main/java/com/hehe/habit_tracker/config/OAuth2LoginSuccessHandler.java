package com.hehe.habit_tracker.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.hehe.habit_tracker.common.AuthProvider;
import com.hehe.habit_tracker.dto.response.AuthenticationResponse;
import com.hehe.habit_tracker.entity.OAuthAccount;
import com.hehe.habit_tracker.entity.Users;
import com.hehe.habit_tracker.repository.OAuthAccountRepository;
import com.hehe.habit_tracker.repository.UserRepository;
import com.hehe.habit_tracker.service.AuthenticationService;
import com.hehe.habit_tracker.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

/**
 * Chạy SAU KHI Spring Security đã tự lo toàn bộ phần "nói chuyện với Google"
 * (redirect /oauth2/authorization/google -> Google login -> đổi code lấy thông tin user).
 * Việc còn lại: map user Google -> Users trong DB, rồi phát JWT của CHÍNH app
 * (giống hệt luồng /auth/login) để phần còn lại của API xử lý nhất quán.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    UserRepository userRepository;
    OAuthAccountRepository oAuthAccountRepository;
    AuthenticationService authenticationService;
    UserService userService;

    @Value("${app.frontend-oauth2-redirect-uri}")
    @NonFinal
    String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 'sub' = ID người dùng bất biến của Google, KHÔNG dùng email làm khóa định danh
        // (email có thể đổi ở phía Google).
        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        Users user = oAuthAccountRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, googleId)
                .map(OAuthAccount::getUser)
                .orElseGet(() -> registerGoogleUser(googleId, email, name));

        // Dùng chung logic phát token với /auth/login: vừa ký access token, vừa
        // đặt cookie refresh token HttpOnly lên response NÀY trước khi redirect.
        AuthenticationResponse tokens = authenticationService.issueTokens(user, response);

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("token", tokens.token())
                .build()
                .toUriString();
        response.sendRedirect(redirectUrl);
    }

    /** Lần đầu đăng nhập Google: tạo Users (không password) + liên kết OAuthAccount. */
    private Users registerGoogleUser(String googleId, String email, String name) {
        // username là unique trong Users nhưng Google không cấp sẵn -> tự sinh (dùng chung
        // logic với UserService.createUser khi đăng ký thường không có username).
        String seed = email != null ? email.substring(0, email.indexOf('@')) : name;
        // Không set password: tài khoản này chỉ đăng nhập qua Google.
        // emailVerified=true: email đã được Google xác minh, không cần bước verify của app.
        Users user = Users.builder()
                .username(userService.generateUniqueUsername(seed))
                .email(email)
                .emailVerified(true)
                .build();
        user = userRepository.save(user);

        OAuthAccount account = OAuthAccount.builder()
                .user(user)
                .provider(AuthProvider.GOOGLE)
                .providerUserId(googleId)
                .email(email)
                .build();
        oAuthAccountRepository.save(account);

        return user;
    }
}
