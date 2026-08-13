import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import type { User } from '@/lib/types';
import { usersApi } from '@/lib/resources';
import { api } from '@/lib/apiClient';
import { clearToken, getToken, setToken } from '@/lib/apiClient';

interface AuthCtx {
  user: User | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  loginWithGoogle: () => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;
  updateUser: (patch: Partial<User>) => Promise<void>;
}

const Ctx = createContext<AuthCtx | null>(null);
const USER_KEY = 'ht-user-id';

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  // Restore session on load.
  // TODO: thay bằng JWT auth thật từ Spring Boot — verify token qua /auth/me.
  useEffect(() => {
    const token = getToken();
    const userId = localStorage.getItem(USER_KEY);
    if (token && userId) {
      usersApi
        .get(userId)
        .then((u) => setUser(u))
        .catch(() => {
          clearToken();
          localStorage.removeItem(USER_KEY);
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const persist = useCallback((u: User) => {
    // Mock JWT — a fake token so the client "has a session".
    setToken(`mock.${u.id}`);
    localStorage.setItem(USER_KEY, u.id);
    setUser(u);
  }, []);

  const login = useCallback(
    async (email: string, password: string) => {
      // TODO: thay bằng JWT auth thật — POST /auth/login rồi lưu token trả về.
      const matches = await usersApi.list({ email: email.trim().toLowerCase() });
      const found = matches.find((u) => u.email.toLowerCase() === email.trim().toLowerCase());
      if (!found) throw new Error('Email chưa được đăng ký');
      if ((found.password ?? '') !== password) throw new Error('Mật khẩu không đúng');
      persist(found);
    },
    [persist],
  );

  const register = useCallback(
    async (name: string, email: string, password: string) => {
      const normalized = email.trim().toLowerCase();
      const existing = await usersApi.list({ email: normalized });
      if (existing.some((u) => u.email.toLowerCase() === normalized)) {
        throw new Error('Email này đã được sử dụng');
      }
      const created = await api.post<User>('/users', {
        name: name.trim(),
        email: normalized,
        password,
        avatar: '',
      });
      persist(created);
    },
    [persist],
  );

  const loginWithGoogle = useCallback(async () => {
    // TODO: thay bằng OAuth Google thật qua Spring Boot.
    // Demo: dùng/khởi tạo một tài khoản Google giả lập.
    const email = 'google.user@habit.app';
    const existing = await usersApi.list({ email });
    const found = existing.find((u) => u.email === email);
    const u =
      found ??
      (await api.post<User>('/users', { name: 'Google User', email, password: '', avatar: '' }));
    persist(u);
  }, [persist]);

  const logout = useCallback(() => {
    clearToken();
    localStorage.removeItem(USER_KEY);
    setUser(null);
  }, []);

  const updateUser = useCallback(
    async (patch: Partial<User>) => {
      if (!user) return;
      const updated = await usersApi.update(user.id, patch);
      setUser(updated);
    },
    [user],
  );

  const value = useMemo<AuthCtx>(
    () => ({ user, loading, login, loginWithGoogle, register, logout, updateUser }),
    [user, loading, login, loginWithGoogle, register, logout, updateUser],
  );

  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useAuth(): AuthCtx {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
