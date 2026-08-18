# Habit Tracker — TODO / Roadmap

> Quy ước: `- [ ]` chưa làm · `- [x]` đã xong. Khi hoàn thành một mục, đổi `[ ]` → `[x]`.
> Ưu tiên: 🔴 P0 (trước khi lên production) · 🟠 P1 (nợ kỹ thuật) · 🟡 P2 (nice to have) · 🚀 tính năng.

---

## ✅ Đã hoàn thành (tham chiếu)

- [x] Auth: JWT ký RSA (resource server + JWKS), self-issue token
- [x] Refresh token: cookie HttpOnly, rotate **tại chỗ** (không phình bảng), cleanup định kỳ
- [x] Đăng nhập Google OAuth2 (backend tự đổi code)
- [x] Chống IDOR / rò dữ liệu chéo user (danh tính từ JWT, không tin client)
- [x] Achievement engine tự cấp khi check-in (STREAK per-habit + MULTI_STREAK account-level)
- [x] Fix N+1 bằng `@EntityGraph` (definition, iconHabit)
- [x] Đọc `userId` từ claim JWT (bỏ query `users` thừa)
- [x] Testcontainers (Postgres) cho CI + GitHub Actions xanh
- [x] Externalize secrets ra `secrets.properties` (gitignored) + xoá mật khẩu khỏi lịch sử git
- [x] Frontend: access token trong RAM + refresh cookie, bỏ mock API

---

## 🔴 P0 — làm trước khi coi là "production-ready"

- [x] **Rotate mật khẩu DB Supabase** — đã đổi mật khẩu mới + cập nhật `secrets.properties`; verify kết nối OK (HikariPool connected), mật khẩu cũ vô hiệu. ✅
- [x] **Thay `ddl-auto: update` bằng Flyway** — `ddl-auto: validate` + Flyway baseline (V1 = schema hiện tại, V2 = thêm zone_id). DB cũ được baseline không mất data; DB mới chạy từ đầu. ✅
- [x] **Lưu timezone của user + tính "hôm nay" theo timezone đó** — thêm cột `zone_id`, `AchievementEngine` tính streak theo `LocalDate.now(userZone)`, frontend gửi timezone lúc đăng ký, fallback `app.default-timezone`. ✅

## 🟠 P1 — nợ kỹ thuật thật

- [x] **Test Achievement engine** (STREAK + MULTI_STREAK) — `AchievementEngineTest` (Mockito, StreakCalculator thật): cấp đúng ngưỡng, không cấp trùng, multi_streak đủ/thiếu habit. ✅
- [x] **Test ownership/IDOR** cho Habit/Checkin/UserAchivement service — sai chủ/không tồn tại → 404, không thao tác được dữ liệu người khác, không gắn thành tựu vào habit người khác. ✅
- [x] **`/checkins/me` lọc theo khoảng ngày** — thêm `?from&to` (ISO); FE gửi cửa sổ 730 ngày gần nhất (đủ cho streak) → payload có trần thay vì phình vô hạn. Dùng unique index (habit_id, checkin_date) sẵn có + thêm index habits(user_id) (V6). ✅
- [x] **Partial unique index cho achievement account-level** (`WHERE habit_id IS NULL`) — V4 migration; DB tự chặn trùng (đã test insert trùng → unique violation), đóng race condition. ✅
- [x] **Logic `weekly_3` / `weekly_5`** — backend `StreakCalculator` giờ đếm theo TUẦN (>= N check-in/tuần, tuần ISO theo thứ Hai), khớp frontend; hết lệch FE/BE khi cấp achievement. +3 unit test. ✅
- [x] **Cột `currentStreak`/`bestStreak`/`lastCheckinDate` trên Habit** — đã XOÁ (V5 migration + bỏ khỏi entity/HabitResponse/FE); streak tính on-the-fly. ✅

## 🟡 P2 — nice to have

- [ ] Rate limiting cho `/auth/login`, `/auth/refresh` (chống brute-force).
- [ ] Error code chuẩn để FE khỏi dò keyword tiếng Việt (message backend đang tiếng Anh).
- [ ] Code-splitting frontend (bundle ~1.5MB).
- [ ] Tắt `show-sql` ở production + thêm actuator health/metrics.
- [ ] Password reset + email verification.
- [x] Bỏ `"user"` khỏi `@EntityGraph` của `HabitRepository.findByUserId` — giờ chỉ fetch `iconHabit`, khỏi load cả hàng `users` (có `password`). ✅

## 🚀 Tính năng đề xuất

- [x] **Nhắc nhở — Web Push** ✅ — VAPID + `WebPushService` + `ReminderScheduler` (mỗi phút, theo timezone user, bỏ qua habit paused/đã check-in) + service worker + toggle ở Profile. (Email nhắc nhở: để sau nếu cần.)
- [ ] **Đồng bộ Google Calendar** — xin thêm scope `calendar.events` (incremental auth) + offline access; tạo recurring event `RRULE` map từ `frequency`; lưu Google refresh token vào `oauth_accounts`.
- [ ] **Hoàn thiện Streak Freeze** — UI đã có toggle, chưa có logic "đóng băng" streak khi lỡ 1 ngày.
- [ ] **Social / leaderboard** — mở rộng từ share card đã có.

---

## Roadmap gợi ý (thứ tự nên làm)

1. Timezone + Flyway (nền tảng cho mọi thứ nhắc nhở).
2. Nhắc nhở (Web Push ưu tiên).
3. Test achievement engine + ownership.
4. Google Calendar (wow factor).
