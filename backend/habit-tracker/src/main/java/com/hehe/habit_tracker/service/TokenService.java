package com.hehe.habit_tracker.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.hehe.habit_tracker.entity.Users;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

/**
 * Phát JWT của chính app (RS256) sau khi đã xác thực người dùng — dùng chung cho
 * mọi đường vào (login thường bằng password, hoặc login Google...). Nơi xác thực
 * là ai (AuthenticationService, OAuth2LoginSuccessHandler) không cần biết chi tiết ký JWT.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenService {

    JwtEncoder jwtEncoder;

    @Value("${jwt.validDuration}")
    @NonFinal
    long validDuration;

    public String generateToken(Users user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("habit-tracker")
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(validDuration))
                .claim("scope", user.getRole().name())
                .claim("userId", user.getId())
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
