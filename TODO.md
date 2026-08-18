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

- [ ] **Rotate mật khẩu DB Supabase** — mật khẩu cũ từng nằm plaintext (file/git/chat) → coi như đã lộ, phải đổi + cập nhật `secrets.properties`.
- [x] **Thay `ddl-auto: update` bằng Flyway** — `ddl-auto: validate` + Flyway baseline (V1 = schema hiện tại, V2 = thêm zone_id). DB cũ được baseline không mất data; DB mới chạy từ đầu. ✅
- [x] **Lưu timezone của user + tính "hôm nay" theo timezone đó** — thêm cột `zone_id`, `AchievementEngine` tính streak theo `LocalDate.now(userZone)`, frontend gửi timezone lúc đăng ký, fallback `app.default-timezone`. ✅

## 🟠 P1 — nợ kỹ thuật thật

- [x] **Test Achievement engine** (STREAK + MULTI_STREAK) — `AchievementEngineTest` (Mockito, StreakCalculator thật): cấp đúng ngưỡng, không cấp trùng, multi_streak đủ/thiếu habit. ✅
- [ ] **Test ownership/IDOR** cho Habit/Checkin/UserAchivement service (sai chủ → 404).
- [ ] **`/checkins/me` phân trang / lọc theo khoảng ngày** — hiện trả TẤT CẢ check-in, tăng vô hạn theo thời gian dùng.
- [ ] **Partial unique index cho achievement account-level** (`WHERE habit_id IS NULL`) — chống trùng đang dựa vào check ở service = còn race condition; cần index DB (làm cùng lúc migrate Flyway).
- [ ] **Logic `weekly_3` / `weekly_5`** — hiện chỉ là nhãn, streak vẫn tính theo ngày; hoặc làm đúng "N lần/tuần", hoặc bỏ preset.
- [ ] **Cột `currentStreak`/`bestStreak`/`lastCheckinDate` trên Habit** — đang không bao giờ được cập nhật (cột chết); maintain hoặc xoá.

## 🟡 P2 — nice to have

- [ ] Rate limiting cho `/auth/login`, `/auth/refresh` (chống brute-force).
- [ ] Error code chuẩn để FE khỏi dò keyword tiếng Việt (message backend đang tiếng Anh).
- [ ] Code-splitting frontend (bundle ~1.5MB).
- [ ] Tắt `show-sql` ở production + thêm actuator health/metrics.
- [ ] Password reset + email verification.
- [ ] Bỏ `"user"` khỏi `@EntityGraph` của `HabitRepository.findByUserId` (đang load cả cột `password` thừa; chỉ cần `iconHabit`).

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
