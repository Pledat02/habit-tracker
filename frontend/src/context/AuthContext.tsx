import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import type { User } from '@/lib/types';
import { usersApi } from '@/lib/resources';
import { api, clearToken, googleLoginUrl, refreshAccessToken, setToken } from '@/lib/apiClient';

interface AuthCtx {
  user: User | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  loginWithGoogle: () => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;
  updateUser: (patch: Partial<User>) => Promise<void>;
  /** Dùng bởi trang /oauth2/callback: nhận token backend đã redirect kèm theo, hoàn tất đăng nhập Google. */
  completeGoogleLogin: (token: string) => Promise<void>;
}

const Ctx = createContext<AuthCtx | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  // Restore session on load. Access token sống trong RAM nên MẤT sau F5 — thử đổi
  // lại bằng refresh token (cookie HttpOnly, trình duyệt tự gửi kèm), rồi lấy
  // profile thật qua GET /users/me (xác định user bằng chính token vừa có).
  useEffect(() => {
    refreshAccessToken()
      .then((token) => {
        if (!token) return;
        return usersApi.me().then((u) => setUser(u));
      })
      .catch(() => clearToken())
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    // Backend chỉ nhận field tên "username" nhưng CHẤP NHẬN cả username lẫn email
    // (xem AuthenticationService.authenticate) — form ở đây thu email, gửi thẳng vào đó.
    const res = await api.post<{ token: string }>('/auth/login', {
      username: email.trim(),
      password,
    });
    setToken(res.token);
    const me = await usersApi.me();
    setUser(me);
  }, []);

  const register = useCallback(
    async (_name: string, email: string, password: string) => {
      // Không gửi username: backend tự sinh từ email khi bỏ trống (UserService.createUser).
      await api.post('/auth/register', { username: null, email: email.trim(), password });
      // Đăng ký xong thì đăng nhập ngay bằng chính thông tin vừa tạo.
      await login(email, password);
    },
    [login],
  );

  const completeGoogleLogin = useCallback(async (token: string) => {
    setToken(token);
    const me = await usersApi.me();
    setUser(me);
  }, []);

  const loginWithGoogle = useCallback(async () => {
    // Rời hẳn SPA để sang trang login Google — quay lại qua /oauth2/callback?token=...
    // (route đó gọi completeGoogleLogin). Promise này không bao giờ resolve vì trang đã điều hướng đi.
    window.location.href = googleLoginUrl();
  }, []);

  const logout = useCallback(() => {
    // Cookie refresh_token là HttpOnly -> JS không tự xoá được, PHẢI nhờ server
    // (Set-Cookie Max-Age=0) + revoke ở DB. Thiếu bước này, "đăng xuất" chỉ là
    // giả — cookie cũ vẫn còn hiệu lực. Best-effort: không chặn logout khi lỗi mạng.
    api.post('/auth/logout', undefined).catch(() => {});
    clearToken();
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
    () => ({ user, loading, login, loginWithGoogle, register, logout, updateUser, completeGoogleLogin }),
    [user, loading, login, loginWithGoogle, register, logout, updateUser, completeGoogleLogin],
  );

  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useAuth(): AuthCtx {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
