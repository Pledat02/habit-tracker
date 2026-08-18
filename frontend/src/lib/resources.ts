import { api } from './apiClient';
import { toDateKey } from './utils';
import type { Achievement, AchievementDefinition, Checkin, Habit, User } from './types';

// Thin resource layer over the generic client. Mỗi API tự dịch qua lại giữa shape
// backend Spring Boot (số id, camelCase khác tên...) và shape frontend đang dùng ở
// khắp UI — để phần còn lại của app (HabitCard, Dashboard, Insights...) không phải đổi gì.

/** Shape thật của UserCreationResponse bên Spring Boot — khác field name với User (username, không có avatar). */
interface BackendUser {
  id: number;
  username: string;
  email: string;
  role: string;
  createdAt: string;
  updatedAt: string;
}

function toUser(u: BackendUser): User {
  return { id: String(u.id), name: u.username, email: u.email, avatar: '' };
}

export const usersApi = {
  update: (id: string, patch: Partial<User>) => api.put<User>(`/users/${id}`, patch),
  /** GET /users/me — user hiện tại, xác định qua Bearer token. */
  me: () => api.get<BackendUser>('/users/me').then(toUser),
};

// ------------------------- Habits --------------------------------------------

/** HabitResponse bên Spring Boot: icon là 2 field phẳng (icon/iconColor), không phải entity lồng. */
interface BackendHabit {
  id: number;
  name: string;
  frequency: string;
  note: string | null;
  remindTime: string | null; // 'HH:mm:ss' hoặc null
  isPaused: boolean;
  icon: string | null;
  iconColor: string | null;
  createdAt: string;
  updatedAt: string;
}

function toHabit(h: BackendHabit): Habit {
  return {
    id: String(h.id),
    userId: '', // backend tự suy chủ sở hữu qua JWT — không còn cần gửi/đọc field này
    name: h.name,
    icon: h.icon ?? 'GlassWater',
    color: h.iconColor ?? 'primary',
    frequency: h.frequency,
    reminderTime: (h.remindTime ?? '').slice(0, 5), // "09:00:00" -> "09:00"
    paused: h.isPaused,
    createdAt: h.createdAt,
  };
}

type HabitInput = Partial<Omit<Habit, 'id' | 'createdAt' | 'userId'>>;

/** Map field frontend (icon/color/reminderTime/paused) -> field backend (icon/iconColor/remindTime/isPaused). */
function fromHabitInput(data: HabitInput): Record<string, unknown> {
  const body: Record<string, unknown> = {};
  if (data.name !== undefined) body.name = data.name;
  if (data.frequency !== undefined) body.frequency = data.frequency;
  if (data.reminderTime !== undefined) body.remindTime = data.reminderTime || null;
  if (data.icon !== undefined) body.icon = data.icon;
  if (data.color !== undefined) body.iconColor = data.color;
  if (data.paused !== undefined) body.isPaused = data.paused;
  return body;
}

export const habitsApi = {
  /** Không còn nhận userId — backend tự lọc theo user trong JWT. */
  list: () => api.get<BackendHabit[]>('/api/v1/habits').then((rows) => rows.map(toHabit)),
  get: (id: string) => api.get<BackendHabit>(`/api/v1/habits/${id}`).then(toHabit),
  create: (data: HabitInput) => api.post<BackendHabit>('/api/v1/habits', fromHabitInput(data)).then(toHabit),
  update: (id: string, patch: HabitInput) =>
    api.put<BackendHabit>(`/api/v1/habits/${id}`, fromHabitInput(patch)).then(toHabit),
  remove: (id: string) => api.delete<void>(`/api/v1/habits/${id}`),
};

// ------------------------- Checkins -------------------------------------------

/** CheckinResponse bên Spring Boot dùng tên field `checkinDate`, không phải `date`. */
interface BackendCheckin {
  id: number;
  habitId: number;
  checkinDate: string; // 'YYYY-MM-DD'
  note: string | null;
  createdAt: string;
}

function toCheckin(c: BackendCheckin): Checkin {
  return { id: String(c.id), habitId: String(c.habitId), date: c.checkinDate, note: c.note ?? '', createdAt: c.createdAt };
}

/** POST /checkins giờ trả { checkin, newAchievements } — engine tự cấp thành tựu ngay khi check-in. */
interface BackendCheckinResult {
  checkin: BackendCheckin;
  newAchievements: BackendUserAchievement[];
}

export interface CheckinResult {
  checkin: Checkin;
  newAchievements: Achievement[];
}

export const checkinsApi = {
  listByHabit: (habitId: string) =>
    api.get<BackendCheckin[]>(`/api/v1/checkins/habit/${habitId}`).then((rows) => rows.map(toCheckin)),
  /**
   * Check-in của user hiện tại (mọi habit gộp lại) — cho Dashboard/Insights.
   * Chỉ lấy cửa sổ 730 ngày gần nhất: đủ cho streak (lookback tối đa 730 ngày) mà không
   * tải toàn bộ lịch sử — payload có trần cố định thay vì phình theo thời gian dùng.
   */
  listAll: () => {
    const to = new Date();
    const from = new Date();
    from.setDate(from.getDate() - 730);
    const q = `from=${toDateKey(from)}&to=${toDateKey(to)}`;
    return api.get<BackendCheckin[]>(`/api/v1/checkins/me?${q}`).then((rows) => rows.map(toCheckin));
  },
  create: (data: { habitId: string; date: string; note?: string }): Promise<CheckinResult> =>
    api
      .post<BackendCheckinResult>('/api/v1/checkins', {
        habitId: Number(data.habitId),
        checkinDate: data.date,
        note: data.note,
      })
      .then((r) => ({ checkin: toCheckin(r.checkin), newAchievements: r.newAchievements.map(toAchievement) })),
  remove: (id: string) => api.delete<void>(`/api/v1/checkins/${id}`),
};

// ------------------------- Achievements ----------------------------------------

interface BackendAchievementDefinition {
  id: number;
  code: string;
  category: 'PER_HABIT' | 'ACCOUNT';
  type: string;
  name: string;
  description: string | null;
  icon: string | null;
  target: number | null;
  sortOrder: number;
  active: boolean;
}

interface BackendUserAchievement {
  id: number;
  userId: number;
  definitionId: number;
  code: string;
  name: string;
  icon: string;
  type: string;
  category: string;
  habitId: number | null;
  unlockedAt: string;
  shared: boolean;
}

function toDefinition(d: BackendAchievementDefinition): AchievementDefinition {
  return {
    id: String(d.id),
    code: d.code,
    category: d.category,
    type: d.type,
    name: d.name,
    description: d.description ?? '',
    icon: d.icon ?? 'Trophy',
    target: d.target,
  };
}

/** Suy milestone từ code kiểu 'STREAK_30' -> 30 (backend không trả số này trực tiếp). */
function milestoneFromCode(code: string): number | null {
  const m = /^STREAK_(\d+)$/.exec(code);
  return m ? Number(m[1]) : null;
}

function toAchievement(a: BackendUserAchievement): Achievement {
  return {
    id: String(a.id),
    habitId: a.habitId !== null ? String(a.habitId) : null,
    code: a.code,
    milestone: milestoneFromCode(a.code),
    unlockedAt: a.unlockedAt,
    shared: a.shared,
  };
}

// ------------------------- Web Push --------------------------------------------

export const pushApi = {
  getPublicKey: () => api.get<string>('/api/v1/push/public-key'),
  subscribe: (data: { endpoint: string; p256dh: string; auth: string }) =>
    api.post<void>('/api/v1/push/subscribe', data),
  unsubscribe: (endpoint: string) =>
    api.delete<void>(`/api/v1/push/subscribe?endpoint=${encodeURIComponent(endpoint)}`),
};

export const achievementsApi = {
  /** Catalog định nghĩa (STREAK_7, STREAK_30...) — dùng để tra definitionId khi mở khoá thành tựu mới. */
  listDefinitions: () =>
    api.get<BackendAchievementDefinition[]>('/api/v1/achievements').then((rows) => rows.map(toDefinition)),
  /** Thành tựu CHÍNH user hiện tại đã mở khoá. */
  list: () => api.get<BackendUserAchievement[]>('/api/v1/user-achievements/me').then((rows) => rows.map(toAchievement)),
  grant: (definitionId: string, habitId?: string) =>
    api
      .post<BackendUserAchievement>('/api/v1/user-achievements', {
        definitionId: Number(definitionId),
        habitId: habitId ? Number(habitId) : null,
      })
      .then(toAchievement),
  markShared: (id: string) =>
    api.put<BackendUserAchievement>(`/api/v1/user-achievements/${id}`, { shared: true }).then(toAchievement),
};
