import { useState } from 'react';
import { MailWarning } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/context/ToastContext';
import { authApi } from '@/lib/resources';

/**
 * Nhắc xác thực email cho user chưa verify. Không chặn thao tác (non-blocking) — chỉ hiện
 * banner + nút gửi lại link. Ẩn hẳn nếu đã verify hoặc backend chưa trả trường này (undefined).
 */
export function VerifyEmailBanner() {
  const { user } = useAuth();
  const toast = useToast();
  const [sending, setSending] = useState(false);

  if (!user || user.emailVerified !== false) return null;

  const resend = async () => {
    setSending(true);
    try {
      await authApi.resendVerification(user.email);
      toast.success('Đã gửi lại email xác thực. Kiểm tra hộp thư (và mục spam).');
    } catch {
      toast.error('Không gửi lại được, thử lại sau ít phút.');
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="mb-5 flex flex-col gap-2 rounded-xl border border-warning bg-warning/10 px-4 py-3 text-sm sm:flex-row sm:items-center sm:justify-between">
      <div className="flex items-center gap-2 text-foreground">
        <MailWarning className="h-4 w-4 shrink-0 text-warning" />
        <span>
          Email <span className="font-medium">{user.email}</span> chưa được xác thực. Kiểm tra hộp thư để
          hoàn tất.
        </span>
      </div>
      <button
        onClick={resend}
        disabled={sending}
        className="focus-ring shrink-0 self-start rounded-lg bg-warning/20 px-3 py-1.5 font-medium text-warning disabled:opacity-60 sm:self-auto"
      >
        {sending ? 'Đang gửi…' : 'Gửi lại link'}
      </button>
    </div>
  );
}
