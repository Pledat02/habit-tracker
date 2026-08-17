import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Loader2 } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';

/**
 * Backend redirect về đây sau khi đăng nhập Google xong, kèm ?token=<JWT>
 * (xem OAuth2LoginSuccessHandler phía Spring Boot). Trang này chỉ có nhiệm vụ
 * đọc token, hoàn tất đăng nhập, rồi điều hướng vào app — không hiển thị gì lâu.
 */
export function OAuth2Callback() {
  const navigate = useNavigate();
  const { completeGoogleLogin } = useAuth();
  const ran = useRef(false); // StrictMode gọi effect 2 lần ở dev — tránh chạy trùng.

  useEffect(() => {
    if (ran.current) return;
    ran.current = true;

    const token = new URLSearchParams(window.location.search).get('token');
    if (!token) {
      navigate('/auth', { replace: true });
      return;
    }
    completeGoogleLogin(token)
      .then(() => navigate('/', { replace: true }))
      .catch(() => navigate('/auth', { replace: true }));
  }, [completeGoogleLogin, navigate]);

  return (
    <div className="flex min-h-[100dvh] items-center justify-center bg-background">
      <Loader2 className="h-8 w-8 animate-spin text-primary" />
    </div>
  );
}
