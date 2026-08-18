/**
 * Centralized API client — the ONLY place that talks to the network.
 * Gọi thẳng backend Spring Boot qua VITE_API_BASE_URL.
 */

const BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.replace(/\/$/, '') ?? '';

/**
 * Access token sống trong RAM (biến module), KHÔNG localStorage.
 * Lý do: localStorage đọc được bởi bất kỳ đoạn JS nào chạy trên trang (kể cả script
 * độc nếu dính XSS) — chỉ 1 dòng `localStorage.getItem(...)`. Biến RAM khó bị đọc
 * trộm hơn nhiều. Đánh đổi: mất khi F5/đóng tab — bù lại bằng refresh token nằm
 * trong cookie HttpOnly (JS không đọc được cookie đó), xem refreshAccessToken().
 */
let accessTokenInMemory: string | null = null;

export function getToken(): string | null {
  return accessTokenInMemory;
}
export function setToken(token: string): void {
  accessTokenInMemory = token;
}
export function clearToken(): void {
  accessTokenInMemory = null;
}

// Gộp nhiều request refresh đồng thời (vd nhiều API gọi cùng lúc đều 401) thành 1
// request /auth/refresh duy nhất, tránh đua nhau rotate refresh token.
let refreshInFlight: Promise<string | null> | null = null;

/** Đổi refresh token (cookie HttpOnly, trình duyệt tự gửi) lấy access token mới. */
export function refreshAccessToken(): Promise<string | null> {
  if (refreshInFlight) return refreshInFlight;

  refreshInFlight = (async () => {
    try {
      const res = await fetch(`${BASE_URL}/auth/refresh`, {
        method: 'POST',
        credentials: 'include', // BẮT BUỘC: để trình duyệt đính kèm cookie refresh_token
      });
      if (!res.ok) {
        clearToken();
        return null;
      }
      const body = (await res.json()) as { data?: { token?: string } };
      const token = body.data?.token ?? null;
      if (token) setToken(token);
      else clearToken();
      return token;
    } catch {
      clearToken();
      return null;
    } finally {
      refreshInFlight = null;
    }
  })();

  return refreshInFlight;
}

export interface QueryParams {
  [key: string]: string | number | undefined;
}

// Endpoint tự nó không cần (và không nên) tự động thử refresh lại khi 401 —
// tránh vòng lặp vô hạn nếu chính /auth/refresh cũng trả 401.
const NO_AUTO_REFRESH_PATHS = ['/auth/login', '/auth/refresh', '/auth/register'];

async function httpRequest<T>(
  method: string,
  path: string,
  body?: unknown,
  params?: QueryParams,
  isRetry = false,
): Promise<T> {
  const url = new URL(BASE_URL + path);
  if (params) {
    for (const [k, v] of Object.entries(params)) {
      if (v !== undefined) url.searchParams.set(k, String(v));
    }
  }

  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(url.toString(), {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
    credentials: 'include', // gửi kèm cookie HttpOnly refresh_token khi gọi /auth/*
  });

  // Access token hết hạn giữa chừng: thử refresh 1 lần rồi gọi lại đúng request này.
  if (res.status === 401 && !isRetry && !NO_AUTO_REFRESH_PATHS.includes(path)) {
    const newToken = await refreshAccessToken();
    if (newToken) return httpRequest<T>(method, path, body, params, true);
  }

  // Backend LUÔN bọc response trong ApiResponse<T> = { data, message, status }
  // (xem common/ApiResponse.java) — kể cả lúc lỗi. Bóc .message để báo lỗi sạch
  // thay vì ném nguyên chuỗi JSON thô ra UI.
  if (!res.ok) {
    const errBody = (await res.json().catch(() => null)) as { message?: string } | null;
    throw new ApiError(res.status, errBody?.message || res.statusText);
  }
  if (res.status === 204) return undefined as T;
  const envelope = (await res.json()) as { data: T };
  return envelope.data;
}

/** URL bắt đầu luồng đăng nhập Google — Spring Security tự redirect từ đây. */
export function googleLoginUrl(): string {
  return `${BASE_URL}/oauth2/authorization/google`;
}

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

export const api = {
  get: <T>(path: string, params?: QueryParams) => httpRequest<T>('GET', path, undefined, params),
  post: <T>(path: string, body: unknown) => httpRequest<T>('POST', path, body),
  put: <T>(path: string, body: unknown) => httpRequest<T>('PUT', path, body),
  delete: <T>(path: string) => httpRequest<T>('DELETE', path),
};
