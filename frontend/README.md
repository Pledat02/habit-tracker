# Habit Tracker — Frontend

Giao diện Habit Tracker: React + TypeScript + Tailwind CSS (Vite). Mobile-first, responsive, dark mode, PWA-ready.

## Chạy nhanh

```bash
npm install
npm run dev
```

Mở http://localhost:5173.

Mặc định app chạy ở **chế độ demo** (không cần backend): dữ liệu lưu trong `localStorage`,
đã seed sẵn vài habit + lịch sử check-in để xem ngay heatmap, streak, biểu đồ.

Tài khoản demo: `demo@habit.app` / `demo1234` (hoặc bấm "Đăng ký" tạo tài khoản mới,
hoặc "Đăng nhập với Google").

## Kết nối backend (MockAPI hoặc Spring Boot)

Chỉ cần đổi **1 biến duy nhất**. Copy `.env.example` -> `.env` và đặt:

```env
# MockAPI:
VITE_API_BASE_URL=https://<project-id>.mockapi.io/api/v1
# hoặc Spring Boot thật:
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

Khi `VITE_API_BASE_URL` có giá trị, mọi request đi qua `src/lib/apiClient.ts` bằng `fetch`.
Khi để trống, dùng mock localStorage tích hợp. **Response shape giống hệt nhau** nên UI không đổi.

### MockAPI: tạo 4 resource
- `users`:        `{ id, name, email, avatar, password }`
- `habits`:       `{ id, userId, name, icon, color, frequency, reminderTime, paused, createdAt }`
- `checkins`:     `{ id, habitId, date, note, createdAt }`
- `achievements`: `{ id, userId, habitId, type, milestone, unlockedAt, shared }`

### Khi Spring Boot xong
1. Đổi `VITE_API_BASE_URL` sang domain backend.
2. Trong `src/context/AuthContext.tsx`: thay phần mock login bằng `POST /auth/login`, lưu JWT.
3. Trong `src/lib/apiClient.ts`: JWT đã được gắn sẵn qua `Authorization: Bearer` khi token
   không phải token mock (`mock.*`). Bỏ phần mock nếu muốn.

Tìm nhanh các điểm cần sửa: search `// TODO: thay bằng JWT`.

## Cấu trúc

```
src/
  lib/          apiClient (điểm cắm backend), resources, types, utils, confetti
  context/      Theme, Toast, Auth
  hooks/        useHabits (React Query: habits, checkins, mutations)
  components/   HabitCard, StreakBadge, CalendarHeatmap, HabitFormModal, ui/*, layout/*
  pages/        Onboarding, Auth, Dashboard, HabitsList, HabitDetail, Insights, Profile
```

## Tính năng
- Onboarding 3 bước + gợi ý habit theo mục tiêu
- Auth (đăng nhập/đăng ký, validation inline, Google demo)
- Dashboard "Hôm nay": progress ring, check-in 1 chạm, confetti khi đạt mốc streak (7/30/100)
- Chi tiết habit: heatmap kiểu GitHub, streak hiện tại/dài nhất, biểu đồ tuần, ghi chú, sửa/xóa/tạm dừng
- Tạo/Sửa habit: icon picker, màu, tần suất, giờ nhắc, tùy chọn nâng cao (progressive disclosure)
- Insights: recap, best/worst, xu hướng 30 ngày, xếp hạng
- Hồ sơ/Cài đặt: đổi tên, nhắc nhở, dark mode, đăng xuất (có xác nhận)
- **Chia sẻ thành tựu (viral):**
  - Tự động hiện modal "Chúc mừng" + confetti khi đạt mốc streak 7 / 30 / 100 / 365 ngày
  - Icon theo mốc: 7 = tia lửa, 30 = lửa lớn, 100 = huy chương, 365 = cúp
  - Nút chia sẻ thủ công tại Chi tiết Habit (cạnh streak) và Insights (recap)
  - `ShareCard` → PNG bằng `html-to-image`, 2 tỷ lệ: vuông 1080×1080 & story 1080×1920
  - Web Share API (kèm ảnh) nếu hỗ trợ, fallback "Tải ảnh" + "Copy ảnh" (Clipboard API)
  - Trang **Thành tựu của tôi**: grid huy hiệu đã/chưa mở khóa, bấm để chia sẻ lại
  - Lưu record `achievements` trên MockAPI (chống hiện lại mốc đã đạt), cập nhật `shared`
- Dark mode song song từ đầu · responsive · skeleton loading · toast · React Query

> Lưu ý về màu trên ShareCard: khi render ra ảnh (canvas), CSS theme token (`rgb(var(--...))`)
> bị mất, nên `ShareCard` cố tình dùng màu hex teal cố định để đảm bảo contrast, không phụ
> thuộc dark/light mode.
```
