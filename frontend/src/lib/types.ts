// Field names/structure mirror exactly what Spring Boot will return
// (camelCase, flat) so swapping the backend needs no UI changes.

export interface User {
  id: string;
  name: string;
  email: string;
  avatar: string;
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

export interface Achievement {
  id: string;
  userId: string;
  habitId: string;
  type: 'streak';
  milestone: number; // one of Milestone
  unlockedAt: string; // ISO
  shared: boolean;
}
