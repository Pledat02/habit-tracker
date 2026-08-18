import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { CheckCircle2, Lock } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { useToast } from '@/context/ToastContext';
import { authApi } from '@/lib/resources';
import { localizeError } from '@/lib/errors';

/** Đọc ?token từ URL (link trong email) -> nhập mật khẩu mới -> gọi /auth/password/reset. */
export function ResetPassword() {
  const [params] = useSearchParams();
  const token = params.get('token') ?? '';
  const navigate = useNavigate();
  const toast = useToast();

  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [errors, setErrors] = useState<{ password?: string; confirm?: string }>({});

  const onSubmit = async (ev: React.FormEvent) => {
    ev.preventDefault();
    const e: typeof errors = {};
    if (password.length < 6) e.password = 'Mật khẩu tối thiểu 6 ký tự.';
    if (confirm !== password) e.confirm = 'Mật khẩu nhập lại không khớp.';
    setErrors(e);
    if (Object.keys(e).length > 0) return;

    setSubmitting(true);
    try {
      await authApi.resetPassword(token, password);
      toast.success('Đặt lại mật khẩu thành công. Hãy đăng nhập lại.');
      navigate('/auth');
    } catch (err) {
      toast.error(localizeError(err).message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="flex min-h-[100dvh] items-center justify-center bg-background px-4 py-10">
      <div className="w-full max-w-md">
        <div className="mb-8 flex flex-col items-center text-center">
          <div className="mb-3 flex h-14 w-14 items-center justify-center rounded-2xl bg-primary text-primary-foreground shadow-soft">
            <CheckCircle2 className="h-7 w-7" />
          </div>
          <h1 className="font-heading text-2xl font-bold text-foreground">Đặt lại mật khẩu</h1>
          <p className="mt-1 text-sm text-muted">Nhập mật khẩu mới cho tài khoản của bạn.</p>
        </div>

        {!token ? (
          <div className="card space-y-3 p-6 text-center">
            <p className="text-sm text-foreground">Link không hợp lệ hoặc thiếu token. Hãy yêu cầu link mới.</p>
            <Link to="/forgot-password" className="text-sm text-primary hover:underline">
              Gửi lại link đặt lại mật khẩu
            </Link>
          </div>
        ) : (
          <form onSubmit={onSubmit} className="card space-y-4 p-6">
            <Input
              label="Mật khẩu mới"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              error={errors.password}
              placeholder="••••••••"
              leftIcon={<Lock className="h-4 w-4" />}
              autoComplete="new-password"
            />
            <Input
              label="Nhập lại mật khẩu"
              type="password"
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
              error={errors.confirm}
              placeholder="••••••••"
              leftIcon={<Lock className="h-4 w-4" />}
              autoComplete="new-password"
            />
            <Button type="submit" size="lg" className="w-full" loading={submitting}>
              Đặt lại mật khẩu
            </Button>
          </form>
        )}
      </div>
    </div>
  );
}
