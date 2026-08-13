import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { CheckCircle2, Lock, Mail, User as UserIcon } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/context/ToastContext';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { USING_MOCK } from '@/lib/apiClient';

type Mode = 'login' | 'register';

interface Errors {
  name?: string;
  email?: string;
  password?: string;
}

const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function Auth() {
  const [mode, setMode] = useState<Mode>('login');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<Errors>({});
  const [submitting, setSubmitting] = useState(false);
  const { login, register, loginWithGoogle } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const validate = (): boolean => {
    const e: Errors = {};
    if (mode === 'register' && !name.trim()) e.name = 'Vui lòng nhập tên của bạn';
    if (!email.trim()) e.email = 'Vui lòng nhập email';
    else if (!emailRe.test(email.trim())) e.email = 'Email không hợp lệ';
    if (!password) e.password = 'Vui lòng nhập mật khẩu';
    else if (password.length < 6) e.password = 'Mật khẩu tối thiểu 6 ký tự';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const onSubmit = async (ev: React.FormEvent) => {
    ev.preventDefault();
    if (!validate()) return;
    setSubmitting(true);
    try {
      if (mode === 'login') await login(email, password);
      else await register(name, email, password);
      navigate('/');
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Có lỗi xảy ra';
      // Attach to the most relevant field when possible.
      if (msg.toLowerCase().includes('mật khẩu')) setErrors({ password: msg });
      else if (msg.toLowerCase().includes('email')) setErrors({ email: msg });
      else toast.error(msg);
    } finally {
      setSubmitting(false);
    }
  };

  const onGoogle = async () => {
    setSubmitting(true);
    try {
      await loginWithGoogle();
      navigate('/');
    } catch {
      toast.error('Đăng nhập Google thất bại');
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
          <h1 className="font-heading text-2xl font-bold text-foreground">
            {mode === 'login' ? 'Chào mừng trở lại' : 'Tạo tài khoản'}
          </h1>
          <p className="mt-1 text-sm text-muted">
            {mode === 'login' ? 'Đăng nhập để tiếp tục hành trình của bạn' : 'Bắt đầu xây dựng thói quen tốt ngay hôm nay'}
          </p>
        </div>

        <form onSubmit={onSubmit} className="card space-y-4 p-6">
          {mode === 'register' && (
            <Input
              label="Họ và tên"
              value={name}
              onChange={(e) => setName(e.target.value)}
              error={errors.name}
              placeholder="Nguyễn Văn A"
              leftIcon={<UserIcon className="h-4 w-4" />}
            />
          )}
          <Input
            label="Email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            error={errors.email}
            placeholder="ban@email.com"
            leftIcon={<Mail className="h-4 w-4" />}
            autoComplete="email"
          />
          <Input
            label="Mật khẩu"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            error={errors.password}
            placeholder="••••••••"
            leftIcon={<Lock className="h-4 w-4" />}
            autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
          />

          <Button type="submit" size="lg" className="w-full" loading={submitting}>
            {mode === 'login' ? 'Đăng nhập' : 'Đăng ký'}
          </Button>

          <div className="flex items-center gap-3 py-1">
            <span className="h-px flex-1 bg-border" />
            <span className="text-xs text-muted">hoặc</span>
            <span className="h-px flex-1 bg-border" />
          </div>

          <Button type="button" variant="outline" size="lg" className="w-full" onClick={onGoogle} disabled={submitting}>
            <GoogleIcon /> Đăng nhập với Google
          </Button>
        </form>

        {USING_MOCK && mode === 'login' && (
          <p className="mt-4 rounded-xl bg-surface-2 px-4 py-3 text-center text-xs text-muted">
            Tài khoản demo: <span className="font-medium text-foreground">demo@habit.app</span> / <span className="font-medium text-foreground">demo1234</span>
          </p>
        )}

        <p className="mt-6 text-center text-sm text-muted">
          {mode === 'login' ? 'Chưa có tài khoản?' : 'Đã có tài khoản?'}{' '}
          <button
            onClick={() => {
              setMode(mode === 'login' ? 'register' : 'login');
              setErrors({});
            }}
            className="focus-ring rounded font-semibold text-primary hover:underline"
          >
            {mode === 'login' ? 'Đăng ký ngay' : 'Đăng nhập'}
          </button>
        </p>
      </div>
    </div>
  );
}

function GoogleIcon() {
  return (
    <svg className="h-5 w-5" viewBox="0 0 24 24" aria-hidden>
      <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" />
      <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84A11 11 0 0 0 12 23z" />
      <path fill="#FBBC05" d="M5.84 14.1a6.6 6.6 0 0 1 0-4.2V7.06H2.18a11 11 0 0 0 0 9.88l3.66-2.84z" />
      <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1A11 11 0 0 0 2.18 7.06l3.66 2.84C6.71 7.31 9.14 5.38 12 5.38z" />
    </svg>
  );
}
