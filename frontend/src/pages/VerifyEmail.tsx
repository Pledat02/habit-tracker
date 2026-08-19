import { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { CheckCircle2, Loader2, XCircle } from 'lucide-react';
import { authApi } from '@/lib/resources';
import { localizeError } from '@/lib/errors';

/** Đọc ?token từ link email -> gọi /auth/email/verify -> hiện kết quả. Tự chạy 1 lần khi mở. */
export function VerifyEmail() {
  const [params] = useSearchParams();
  const token = params.get('token') ?? '';
  const [state, setState] = useState<'loading' | 'ok' | 'error'>(token ? 'loading' : 'error');
  const [message, setMessage] = useState(token ? '' : 'Link không hợp lệ hoặc thiếu token.');
  const ran = useRef(false);

  useEffect(() => {
    if (!token || ran.current) return;
    ran.current = true; // StrictMode gọi effect 2 lần -> chặn verify token 2 lần (lần 2 sẽ báo đã dùng).
    authApi
      .verifyEmail(token)
      .then(() => setState('ok'))
      .catch((err) => {
        setState('error');
        setMessage(localizeError(err).message);
      });
  }, [token]);

  return (
    <div className="flex min-h-[100dvh] items-center justify-center bg-background px-4 py-10">
      <div className="w-full max-w-md text-center">
        <div className="card space-y-4 p-8">
          {state === 'loading' && (
            <>
              <Loader2 className="mx-auto h-10 w-10 animate-spin text-primary" />
              <p className="text-sm text-muted">Đang xác thực email…</p>
            </>
          )}
          {state === 'ok' && (
            <>
              <CheckCircle2 className="mx-auto h-12 w-12 text-success" />
              <h1 className="font-heading text-xl font-bold text-foreground">Email đã được xác thực</h1>
              <p className="text-sm text-muted">Cảm ơn bạn! Tài khoản của bạn đã sẵn sàng.</p>
              <Link to="/" className="inline-block text-sm text-primary hover:underline">
                Vào ứng dụng
              </Link>
            </>
          )}
          {state === 'error' && (
            <>
              <XCircle className="mx-auto h-12 w-12 text-danger" />
              <h1 className="font-heading text-xl font-bold text-foreground">Không xác thực được</h1>
              <p className="text-sm text-muted">{message}</p>
              <Link to="/" className="inline-block text-sm text-primary hover:underline">
                Về trang chủ
              </Link>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
