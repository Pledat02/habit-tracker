# Habit Tracker

Ứng dụng theo dõi thói quen: xây dựng và duy trì habit qua check-in hàng ngày, streak,
thống kê trực quan và chia sẻ thành tựu.

Repo dạng monorepo, chia làm 2 phần:

```
habit-tracker/
├── frontend/   # React + TypeScript + Tailwind (Vite) — đã hoàn thiện
└── backend/    # Spring Boot — sẽ bổ sung sau (hiện đang trống)
```

## Frontend

React + TypeScript + Tailwind CSS. Mobile-first, responsive, dark mode, PWA-ready.

```bash
cd frontend
npm install
npm run dev
```

Mở http://localhost:5173. Mặc định chạy **chế độ demo** (dữ liệu lưu trong localStorage,
không cần backend). Tài khoản demo: `demo@habit.app` / `demo1234`.

Chi tiết: xem [frontend/README.md](frontend/README.md).

## Backend

Chưa triển khai. Khi làm xong, frontend chỉ cần đổi 1 biến `VITE_API_BASE_URL`
(trong `frontend/.env`) để trỏ sang backend, không phải sửa UI.

Hợp đồng API dự kiến (4 resource, JSON phẳng camelCase):
- `users`, `habits`, `checkins`, `achievements`

Xem schema chi tiết trong [frontend/README.md](frontend/README.md).
