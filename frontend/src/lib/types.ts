// Field names/structure mirror exactly what Spring Boot will return
// (camelCase, flat) so swapping the backend needs no UI changes.

export interface User {
  id: string;
  name: string;
  email: string;
  avatar: string;
  /** Email đã xác thực chưa (backend trả trong /users/me). Undefined ở dữ liệu mock cũ. */
  emailVerified?: boolean;
  // Note: password only exists in the mock/MockAPI "users" resource for fake login.
  // Real backend never returns this.
  password?: string;
}

export type Frequency =
  | 'daily'
  | 'weekly_3'
  | 'weekly_5'
  | { type: 'days'; days: number[] }; // 0=Sun ... 6=Sat

export interface Habit {
  id: string;
  userId: string;
  name: string;
  icon: string; // lucide icon name
  color: string; // token key: 'primary' | 'secondary' | 'success' | 'warning' | 'danger'
  frequency: string; // JSON-encoded Frequency (kept as string to match a flat backend schema)
  reminderTime: string; // 'HH:mm' or ''
  paused?: boolean;
  createdAt: string; // ISO
}

export interface Checkin {
  id: string;
  habitId: string;
  date: string; // 'YYYY-MM-DD'
  note: string;
  createdAt: string; // ISO
}

export type Milestone = 7 | 30 | 100 | 365;

/**
 * Bản ghi thành tựu ĐÃ MỞ KHOÁ của user hiện tại (UserAchivement bên backend).
 * `milestone` được suy ra CLIENT-SIDE từ `code` (vd 'STREAK_30' -> 30) — backend
 * không trả số milestone trực tiếp, chỉ trả code + các field hiển thị.
 * habitId = null nghĩa là thành tựu account-level (chưa dùng tới ở bản này).
 */
export interface Achievement {
  id: string;
  habitId: string | null;
  code: string;
  milestone: number | null;
  unlockedAt: string; // ISO
  shared: boolean;
}

/** Định nghĩa 1 loại thành tựu trong catalog (Achivement bên backend, đọc-only ở đây). */
export interface AchievementDefinition {
  id: string;
  code: string;
  category: 'PER_HABIT' | 'ACCOUNT';
  type: string;
  name: string;
  description: string;
  icon: string;
  target: number | null;
}
