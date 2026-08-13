/**
 * Centralized API client — the ONLY place that talks to the network.
 *
 * Backend swap plan:
 *  1. Set VITE_API_BASE_URL to your MockAPI or Spring Boot base URL.
 *  2. When Spring Boot is ready: replace the mock login in AuthContext with a
 *     real /auth/login call and attach the JWT below (see AUTH_TOKEN handling).
 *
 * If VITE_API_BASE_URL is empty, requests are served from an in-browser
 * localStorage store that mimics MockAPI's REST semantics (GET/POST/PUT/DELETE
 * + ?field=value filtering). This lets the whole app demo real CRUD flows
 * with zero external setup. Response shapes are identical either way.
 */

import type { Achievement, Checkin, Habit, User } from './types';
import { uid } from './utils';

const BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.replace(/\/$/, '') ?? '';
export const USING_MOCK = BASE_URL === '';

const TOKEN_KEY = 'ht-token';

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}
export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}
export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

export interface QueryParams {
  [key: string]: string | number | undefined;
}

type Resource = 'users' | 'habits' | 'checkins' | 'achievements';

// ------------------------- Real HTTP transport --------------------------------

async function httpRequest<T>(
  method: string,
  path: string,
  body?: unknown,
  params?: QueryParams,
): Promise<T> {
  const url = new URL(BASE_URL + path);
  if (params) {
    for (const [k, v] of Object.entries(params)) {
      if (v !== undefined) url.searchParams.set(k, String(v));
    }
  }

  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  // TODO: thay bằng JWT auth thật từ Spring Boot — gửi token qua Authorization header.
  const token = getToken();
  if (token && !token.startsWith('mock.')) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(url.toString(), {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new ApiError(res.status, text || res.statusText);
  }
  if (res.status === 204) return undefined as T;
  return (await res.json()) as T;
}

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

// ------------------------- Mock localStorage transport ------------------------

const MOCK_KEY = 'ht-mock-db';

interface MockDb {
  users: User[];
  habits: Habit[];
  checkins: Checkin[];
  achievements: Achievement[];
}

function loadDb(): MockDb {
  const raw = localStorage.getItem(MOCK_KEY);
  if (raw) {
    try {
      const db = JSON.parse(raw) as Partial<MockDb>;
      // Migrate DBs seeded before a resource existed.
      if (!db.users) db.users = [];
      if (!db.habits) db.habits = [];
      if (!db.checkins) db.checkins = [];
      if (!db.achievements) db.achievements = [];
      return db as MockDb;
    } catch {
      /* fall through to seed */
    }
  }
  const seeded = seedDb();
  localStorage.setItem(MOCK_KEY, JSON.stringify(seeded));
  return seeded;
}

function saveDb(db: MockDb): void {
  localStorage.setItem(MOCK_KEY, JSON.stringify(db));
}

function nowIso(): string {
  return new Date().toISOString();
}

/** A small, realistic seed so the demo isn't empty on first run. */
function seedDb(): MockDb {
  const userId = 'u1';
  const user: User = {
    id: userId,
    name: 'Minh Anh',
    email: 'demo@habit.app',
    password: 'demo1234',
    avatar: '',
  };

  const mk = (
    id: string,
    name: string,
    icon: string,
    color: string,
    frequency: string,
    reminderTime: string,
  ): Habit => ({
    id,
    userId,
    name,
    icon,
    color,
    frequency,
    reminderTime,
    paused: false,
    createdAt: nowIso(),
  });

  const habits: Habit[] = [
    mk('h1', 'Uống 2L nước', 'GlassWater', 'primary', '"daily"', '09:00'),
    mk('h2', 'Đọc sách 20 phút', 'BookOpen', 'secondary', '"daily"', '21:30'),
    mk('h3', 'Tập thể dục', 'Dumbbell', 'warning', '"weekly_3"', '06:30'),
    mk('h4', 'Thiền 10 phút', 'Brain', 'success', '"daily"', ''),
  ];

  // Generate check-ins for the past ~40 days with varied consistency.
  const checkins: Checkin[] = [];
  const today = new Date();
  const density: Record<string, number> = { h1: 0.9, h2: 0.7, h3: 0.5, h4: 0.6 };
  for (const h of habits) {
    for (let i = 1; i <= 40; i++) {
      const d = new Date(today);
      d.setDate(d.getDate() - i);
      if (Math.random() < density[h.id]) {
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        checkins.push({
          id: uid(),
          habitId: h.id,
          date: `${y}-${m}-${day}`,
          note: '',
          createdAt: d.toISOString(),
        });
      }
    }
  }

  return { users: [user], habits, checkins, achievements: [] };
}

function matchesParams(item: Record<string, unknown>, params?: QueryParams): boolean {
  if (!params) return true;
  return Object.entries(params).every(([k, v]) => {
    if (v === undefined || k === 'sortBy' || k === 'order') return true;
    return String(item[k]) === String(v);
  });
}

async function mockRequest<T>(
  method: string,
  path: string,
  body?: unknown,
  params?: QueryParams,
): Promise<T> {
  // Simulate a little latency so loading states are visible.
  await new Promise((r) => setTimeout(r, 180));

  const db = loadDb();
  const segments = path.split('/').filter(Boolean); // e.g. ['habits'] or ['habits','h1']
  const resource = segments[0] as Resource;
  const id = segments[1];
  const list = db[resource] as unknown as Record<string, unknown>[];

  switch (method) {
    case 'GET': {
      if (id) {
        const found = list.find((x) => x.id === id);
        if (!found) throw new ApiError(404, 'Not found');
        return found as T;
      }
      return list.filter((x) => matchesParams(x, params)) as unknown as T;
    }
    case 'POST': {
      const created = { ...(body as object), id: uid(), createdAt: nowIso() } as Record<string, unknown>;
      list.push(created);
      saveDb(db);
      return created as T;
    }
    case 'PUT': {
      const idx = list.findIndex((x) => x.id === id);
      if (idx === -1) throw new ApiError(404, 'Not found');
      list[idx] = { ...list[idx], ...(body as object), id };
      saveDb(db);
      return list[idx] as T;
    }
    case 'DELETE': {
      const idx = list.findIndex((x) => x.id === id);
      if (idx !== -1) {
        const [removed] = list.splice(idx, 1);
        // Cascade: deleting a habit removes its check-ins and achievements.
        if (resource === 'habits') {
          db.checkins = db.checkins.filter((c) => c.habitId === id);
          db.achievements = db.achievements.filter((a) => a.habitId !== id);
        }
        saveDb(db);
        return removed as T;
      }
      return undefined as T;
    }
    default:
      throw new ApiError(400, `Unsupported method ${method}`);
  }
}

// ------------------------- Public API ----------------------------------------

function request<T>(method: string, path: string, body?: unknown, params?: QueryParams): Promise<T> {
  return USING_MOCK ? mockRequest<T>(method, path, body, params) : httpRequest<T>(method, path, body, params);
}

export const api = {
  get: <T>(path: string, params?: QueryParams) => request<T>('GET', path, undefined, params),
  post: <T>(path: string, body: unknown) => request<T>('POST', path, body),
  put: <T>(path: string, body: unknown) => request<T>('PUT', path, body),
  delete: <T>(path: string) => request<T>('DELETE', path),
};
