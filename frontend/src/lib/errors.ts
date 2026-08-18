import { ApiError } from './apiClient';

/**
 * Map mã lỗi máy-đọc từ backend (ErrorCode.name()) -> thông điệp tiếng Việt + field liên quan.
 * Nhờ mã ổn định, UI không phải dò chuỗi message (message backend là tiếng Anh và có thể đổi).
 */
type FieldHint = 'email' | 'password' | 'name';

interface Localized {
  message: string;
  field?: FieldHint;
}

const CODE_MAP: Record<string, Localized> = {
  USER_EXISTED: { message: 'Tài khoản đã tồn tại.', field: 'email' },
  USER_NOT_EXISTED: { message: 'Tài khoản không tồn tại.', field: 'email' },
  UNAUTHENTICATED: { message: 'Email hoặc mật khẩu không đúng.', field: 'password' },
  REFRESH_TOKEN_INVALID: { message: 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại.' },
  UNAUTHORIZED: { message: 'Bạn không có quyền thực hiện thao tác này.' },
  CHECKIN_EXISTED: { message: 'Hôm nay bạn đã check-in thói quen này rồi.' },
  HABIT_NOT_FOUND: { message: 'Không tìm thấy thói quen.' },
  CHECKIN_NOT_FOUND: { message: 'Không tìm thấy lượt check-in.' },
  ACHIEVEMENT_NOT_FOUND: { message: 'Không tìm thấy thành tựu.' },
  USER_ACHIEVEMENT_EXISTED: { message: 'Thành tựu này đã được mở khoá.' },
  RATE_LIMITED: { message: 'Bạn thao tác quá nhanh, vui lòng thử lại sau ít phút.' },
  INVALID_REQUEST: { message: 'Dữ liệu không hợp lệ.' },
};

/** Dịch một lỗi bất kỳ ra tiếng Việt + field (nếu có) để gắn lỗi đúng ô nhập. */
export function localizeError(err: unknown): Localized {
  if (err instanceof ApiError && err.code && CODE_MAP[err.code]) {
    return CODE_MAP[err.code];
  }
  // Lỗi validation của backend (INVALID_REQUEST) mang message cụ thể theo field -> giữ nguyên message đó.
  if (err instanceof ApiError && err.message) {
    return { message: err.message };
  }
  return { message: err instanceof Error ? err.message : 'Có lỗi xảy ra, vui lòng thử lại.' };
}
