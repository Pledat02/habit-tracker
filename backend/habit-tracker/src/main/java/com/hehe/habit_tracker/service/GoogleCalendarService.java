package com.hehe.habit_tracker.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.hehe.habit_tracker.entity.GoogleCalendarConnection;
import com.hehe.habit_tracker.entity.Habit;
import com.hehe.habit_tracker.entity.Users;
import com.hehe.habit_tracker.exception.AppException;
import com.hehe.habit_tracker.exception.ErrorCode;
import com.hehe.habit_tracker.repository.GoogleCalendarConnectionRepository;
import com.hehe.habit_tracker.repository.HabitRepository;
import com.hehe.habit_tracker.repository.UserRepository;

import java.util.Base64;

import lombok.extern.slf4j.Slf4j;

/**
 * Đồng bộ 1 chiều habit -> Google Calendar (RFC 5545 RRULE), gọi Calendar API bằng RestClient
 * (không dùng thư viện Google nặng). TẤT CẢ hành vi mạng đều sau feature flag
 * {@code app.google-calendar.enabled} và chỉ chạy khi user đã kết nối (có refresh token).
 *
 * Đồng bộ là BEST-EFFORT: mọi lỗi Calendar được nuốt + log, KHÔNG bao giờ làm hỏng CRUD habit.
 */
@Service
@Slf4j
public class GoogleCalendarService {

    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String AUTH_URI = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String EVENTS_URI = "https://www.googleapis.com/calendar/v3/calendars/primary/events";
    private static final String SCOPE = "https://www.googleapis.com/auth/calendar.events";

    private final GoogleCalendarConnectionRepository connectionRepository;
    private final HabitRepository habitRepository;
    private final UserRepository userRepository;
    private final RRuleBuilder rRuleBuilder;
    private final RestClient rest = RestClient.create();

    @Value("${app.google-calendar.enabled:false}")
    private boolean enabled;
    @Value("${app.google-calendar.client-id:}")
    private String clientId;
    @Value("${app.google-calendar.client-secret:}")
    private String clientSecret;
    @Value("${app.google-calendar.redirect-uri:http://localhost:8080/api/v1/calendar/callback}")
    private String redirectUri;
    @Value("${app.google-calendar.state-secret:dev-calendar-state-secret}")
    private String stateSecret;
    @Value("${app.default-timezone:UTC}")
    private String defaultTimezone;
    @Value("${app.google-calendar.event-duration-minutes:30}")
    private int eventDurationMinutes;

    public GoogleCalendarService(GoogleCalendarConnectionRepository connectionRepository,
            HabitRepository habitRepository, UserRepository userRepository, RRuleBuilder rRuleBuilder) {
        this.connectionRepository = connectionRepository;
        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
        this.rRuleBuilder = rRuleBuilder;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isConnected(Long userId) {
        return connectionRepository.existsByUserId(userId);
    }

    // ---------------------------------------------------------------- OAuth connect flow

    /** URL để redirect user sang Google xin quyền Calendar (offline -> có refresh token). */
    public String buildAuthorizeUrl(Long userId) {
        requireEnabled();
        return UriComponentsBuilder.fromUriString(AUTH_URI)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", SCOPE)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent") // luôn trả refresh_token, kể cả đã cấp quyền trước
                .queryParam("include_granted_scopes", "true")
                .queryParam("state", signState(userId))
                .build().toUriString();
    }

    /** Google gọi lại kèm code + state. Đổi code lấy refresh token, lưu kết nối cho user. */
    @Transactional
    public void handleCallback(String code, String state) {
        requireEnabled();
        Long userId = verifyState(state);
        TokenResponse token = exchangeCode(code);
        if (token == null || token.refresh_token() == null) {
            throw new AppException(ErrorCode.CALENDAR_AUTH_FAILED);
        }
        Users user = userRepository.getReferenceById(userId);
        GoogleCalendarConnection conn = connectionRepository.findByUserId(userId)
                .orElseGet(() -> GoogleCalendarConnection.builder().user(user).build());
        conn.setRefreshToken(token.refresh_token());
        connectionRepository.save(conn);
        log.info("User {} đã kết nối Google Calendar", userId);
    }

    @Transactional
    public void disconnect(Long userId) {
        connectionRepository.deleteByUserId(userId);
    }

    // ---------------------------------------------------------------- Sync habit -> event

    /** Đồng bộ 1 habit: tạo/cập nhật event nếu nên có; xoá event nếu không. Best-effort. */
    @Transactional
    public void syncHabit(Habit habit) {
        if (!enabled) {
            return;
        }
        try {
            Long userId = habit.getUser().getId();
            GoogleCalendarConnection conn = connectionRepository.findByUserId(userId).orElse(null);
            if (conn == null) {
                return; // user chưa kết nối -> không làm gì
            }
            boolean shouldExist = !habit.isPaused();
            if (!shouldExist) {
                deleteEventIfAny(habit, conn);
                return;
            }
            String accessToken = accessTokenFor(conn.getRefreshToken());
            Map<String, Object> body = buildEventBody(habit);
            String eventId = habit.getGoogleCalendarEventId();
            if (eventId == null || eventId.isBlank()) {
                EventResponse created = rest.post().uri(EVENTS_URI)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body).retrieve().body(EventResponse.class);
                if (created != null && created.id() != null) {
                    habit.setGoogleCalendarEventId(created.id());
                    habitRepository.save(habit);
                }
            } else {
                rest.put().uri(EVENTS_URI + "/" + eventId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body).retrieve().toBodilessEntity();
            }
        } catch (Exception e) {
            log.error("Đồng bộ Calendar cho habit {} lỗi (bỏ qua): {}", habit.getId(), e.getMessage());
        }
    }

    /** Xoá event của habit (khi habit bị xoá). Best-effort. */
    @Transactional
    public void deleteEventForHabit(Habit habit) {
        if (!enabled || habit.getGoogleCalendarEventId() == null) {
            return;
        }
        try {
            GoogleCalendarConnection conn = connectionRepository.findByUserId(habit.getUser().getId()).orElse(null);
            if (conn != null) {
                deleteEventIfAny(habit, conn);
            }
        } catch (Exception e) {
            log.error("Xoá event Calendar cho habit {} lỗi (bỏ qua): {}", habit.getId(), e.getMessage());
        }
    }

    /** Đồng bộ toàn bộ habit của 1 user (dùng ngay sau khi kết nối). */
    @Transactional
    public void syncAllForUser(Long userId) {
        if (!enabled || !isConnected(userId)) {
            return;
        }
        for (Habit habit : habitRepository.findByUserId(userId)) {
            syncHabit(habit);
        }
    }

    private void deleteEventIfAny(Habit habit, GoogleCalendarConnection conn) {
        String eventId = habit.getGoogleCalendarEventId();
        if (eventId == null || eventId.isBlank()) {
            return;
        }
        try {
            String accessToken = accessTokenFor(conn.getRefreshToken());
            rest.delete().uri(EVENTS_URI + "/" + eventId)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.warn("Xoá event {} lỗi (bỏ qua): {}", eventId, e.getMessage());
        }
        habit.setGoogleCalendarEventId(null);
        habitRepository.save(habit);
    }

    private Map<String, Object> buildEventBody(Habit habit) {
        ZoneId zone = resolveZone(habit.getUser());
        LocalTime time = habit.getRemindTime() != null ? habit.getRemindTime() : LocalTime.of(9, 0);
        LocalDate startDate = LocalDate.now(zone);
        String startDateTime = startDate.atTime(time).toString(); // yyyy-MM-ddTHH:mm:ss (local, không offset)
        String endDateTime = startDate.atTime(time).plusMinutes(eventDurationMinutes).toString();

        return Map.of(
                "summary", "Thói quen: " + habit.getName(),
                "description", "Nhắc từ Habit Tracker.",
                "start", Map.of("dateTime", startDateTime, "timeZone", zone.getId()),
                "end", Map.of("dateTime", endDateTime, "timeZone", zone.getId()),
                "recurrence", List.of(rRuleBuilder.toRRule(habit.getFrequency())),
                "reminders", Map.of("useDefault", false,
                        "overrides", List.of(Map.of("method", "popup", "minutes", 0))));
    }

    private ZoneId resolveZone(Users user) {
        try {
            String zid = user.getZoneId();
            return (zid != null && !zid.isBlank()) ? ZoneId.of(zid) : ZoneId.of(defaultTimezone);
        } catch (Exception e) {
            return ZoneId.of(defaultTimezone);
        }
    }

    // ---------------------------------------------------------------- Google token calls

    private TokenResponse exchangeCode(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("grant_type", "authorization_code");
        return rest.post().uri(TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form).retrieve().body(TokenResponse.class);
    }

    private String accessTokenFor(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refreshToken);
        form.add("grant_type", "refresh_token");
        TokenResponse token = rest.post().uri(TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form).retrieve().body(TokenResponse.class);
        if (token == null || token.access_token() == null) {
            throw new AppException(ErrorCode.CALENDAR_AUTH_FAILED);
        }
        return token.access_token();
    }

    // ---------------------------------------------------------------- state HMAC (chống giả mạo userId)

    private String signState(Long userId) {
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(userId.toString().getBytes(StandardCharsets.UTF_8));
        return payload + "." + hmac(payload);
    }

    private Long verifyState(String state) {
        if (state == null || !state.contains(".")) {
            throw new AppException(ErrorCode.CALENDAR_STATE_INVALID);
        }
        String[] parts = state.split("\\.", 2);
        if (!hmac(parts[0]).equals(parts[1])) {
            throw new AppException(ErrorCode.CALENDAR_STATE_INVALID);
        }
        try {
            return Long.parseLong(new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new AppException(ErrorCode.CALENDAR_STATE_INVALID);
        }
    }

    private String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(stateSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private void requireEnabled() {
        if (!enabled) {
            throw new AppException(ErrorCode.CALENDAR_DISABLED);
        }
    }

    // Response records: RestClient (jackson runtime) map theo tên field snake_case của Google.
    record TokenResponse(String access_token, String refresh_token, Integer expires_in, String scope,
            String token_type) {
    }

    record EventResponse(String id) {
    }
}
