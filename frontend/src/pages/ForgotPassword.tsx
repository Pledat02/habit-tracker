import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowLeft, CheckCircle2, Mail } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { authApi } from '@/lib/resources';
import { localizeError } from '@/lib/errors';

/** Nhập email -> xin link đặt lại mật khẩu. Backend luôn trả 200 nên UI luôn báo "đã gửi". */
export function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [sent, setSent] = useState(false);
  const [error, setError] = useState<string | undefined>();

  const onSubmit = async (ev: React.FormEvent) => {
    ev.preventDefault();
    if (!email.trim()) {
      setError('Vui lòng nhập email.');
      return;
    }
    setSubmitting(true);
    setError(undefined);
    try {
      await authApi.forgotPassword(email.trim());
      setSent(true);
    } catch (err) {
      setError(localizeError(err).message);
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
          <h1 className="font-heading text-2xl font-bold text-foreground">Quên mật khẩu</h1>
          <p className="mt-1 text-sm text-muted">Nhập email, chúng tôi sẽ gửi link đặt lại mật khẩu.</p>
        </div>

        {sent ? (
          <div className="card space-y-3 p-6 text-center">
            <p className="text-sm text-foreground">
              Nếu email <span className="font-medium">{email.trim()}</span> có tài khoản, một link đặt lại mật
              khẩu đã được gửi tới. Kiểm tra hộp thư (và cả mục spam).
            </p>
            <Link to="/auth" className="inline-flex items-center gap-1 text-sm text-primary hover:underline">
              <ArrowLeft className="h-4 w-4" /> Quay lại đăng nhập
            </Link>
          </div>
        ) : (
          <form onSubmit={onSubmit} className="card space-y-4 p-6">
            <Input
              label="Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              error={error}
              placeholder="ban@email.com"
              leftIcon={<Mail className="h-4 w-4" />}
              autoComplete="email"
            />
            <Button type="submit" size="lg" className="w-full" loading={submitting}>
              Gửi link đặt lại
            </Button>
            <Link to="/auth" className="flex items-center justify-center gap-1 text-sm text-muted hover:text-foreground">
              <ArrowLeft className="h-4 w-4" /> Quay lại đăng nhập
            </Link>
          </form>
        )}
      </div>
    </div>
  );
}
