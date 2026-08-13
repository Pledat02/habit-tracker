import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Bell, ChevronRight, LogOut, Mail, Moon, Sun, Trophy, User as UserIcon, Lock, Smartphone } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { useTheme } from '@/context/ThemeContext';
import { useToast } from '@/context/ToastContext';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { ConfirmDialog } from '@/components/ui/ConfirmDialog';
import { cn, initials } from '@/lib/utils';

export function Profile() {
  const { user, logout, updateUser } = useAuth();
  const { theme, toggle } = useTheme();
  const toast = useToast();

  const [name, setName] = useState(user?.name ?? '');
  const [saving, setSaving] = useState(false);
  const [confirmLogout, setConfirmLogout] = useState(false);

  // Reminder preferences (local-only demo state)
  const [emailReminder, setEmailReminder] = useState(true);
  const [pushReminder, setPushReminder] = useState(false);

  const saveProfile = async () => {
    if (!name.trim()) return;
    setSaving(true);
    try {
      await updateUser({ name: name.trim() });
      toast.success('Đã cập nhật hồ sơ');
    } catch {
      toast.error('Lưu thất bại');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <h1 className="text-2xl font-bold text-foreground sm:text-3xl">Hồ sơ &amp; Cài đặt</h1>

      {/* Profile */}
      <section className="card p-5">
        <div className="mb-5 flex items-center gap-4">
          <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-primary/10 text-lg font-bold text-primary">
            {initials(user?.name ?? 'U')}
          </div>
          <div>
            <p className="font-semibold text-foreground">{user?.name}</p>
            <p className="text-sm text-muted">{user?.email}</p>
          </div>
        </div>
        <div className="space-y-4">
          <Input label="Tên hiển thị" value={name} onChange={(e) => setName(e.target.value)} leftIcon={<UserIcon className="h-4 w-4" />} />
          <Input label="Email" value={user?.email ?? ''} disabled leftIcon={<Mail className="h-4 w-4" />} hint="Email không thể thay đổi trong bản demo." />
          <Button onClick={saveProfile} loading={saving} disabled={name.trim() === user?.name}>
            Lưu thay đổi
          </Button>
        </div>
      </section>

      {/* Quick link: achievements (mobile-friendly entry) */}
      <Link
        to="/achievements"
        className="focus-ring card flex items-center gap-3 p-4 transition-shadow hover:shadow-soft-lg"
      >
        <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary/10 text-primary">
          <Trophy className="h-5 w-5" />
        </span>
        <span className="flex-1">
          <span className="block text-sm font-semibold text-foreground">Thành tựu của tôi</span>
          <span className="block text-xs text-muted">Xem &amp; chia sẻ các huy hiệu đã mở khóa</span>
        </span>
        <ChevronRight className="h-5 w-5 text-muted" />
      </Link>

      {/* Security */}
      <section className="card p-5">
        <h2 className="mb-4 flex items-center gap-2 text-sm font-semibold text-foreground">
          <Lock className="h-4 w-4 text-muted" /> Bảo mật
        </h2>
        <Button variant="outline" onClick={() => toast.info('Tính năng đổi mật khẩu sẽ có khi kết nối backend thật.')}>
          Đổi mật khẩu
        </Button>
      </section>

      {/* Appearance */}
      <section className="card p-5">
        <h2 className="mb-4 text-sm font-semibold text-foreground">Giao diện</h2>
        <button
          onClick={toggle}
          className="focus-ring flex w-full items-center justify-between rounded-xl bg-surface-2 p-4"
        >
          <span className="flex items-center gap-3">
            {theme === 'dark' ? <Moon className="h-5 w-5 text-secondary" /> : <Sun className="h-5 w-5 text-warning" />}
            <span className="text-sm font-medium text-foreground">Chế độ tối</span>
          </span>
          <span className={cn('relative inline-flex h-6 w-11 items-center rounded-full transition-colors', theme === 'dark' ? 'bg-primary' : 'bg-border')}>
            <span className={cn('inline-block h-5 w-5 rounded-full bg-white shadow transition-transform', theme === 'dark' ? 'translate-x-5' : 'translate-x-0.5')} />
          </span>
        </button>
      </section>

      {/* Reminders */}
      <section className="card p-5">
        <h2 className="mb-4 flex items-center gap-2 text-sm font-semibold text-foreground">
          <Bell className="h-4 w-4 text-muted" /> Nhắc nhở
        </h2>
        <div className="space-y-2">
          <SettingToggle
            icon={<Mail className="h-5 w-5 text-secondary" />}
            label="Nhắc qua email"
            desc="Nhận email khi sắp đến giờ habit"
            checked={emailReminder}
            onChange={setEmailReminder}
          />
          <SettingToggle
            icon={<Smartphone className="h-5 w-5 text-primary" />}
            label="Thông báo đẩy (push)"
            desc="Nhận thông báo trên thiết bị"
            checked={pushReminder}
            onChange={setPushReminder}
          />
        </div>
      </section>

      {/* Logout — separated, destructive confirm */}
      <section className="card p-5">
        <Button variant="danger" className="w-full" onClick={() => setConfirmLogout(true)}>
          <LogOut className="h-4 w-4" /> Đăng xuất
        </Button>
      </section>

      <ConfirmDialog
        open={confirmLogout}
        title="Đăng xuất?"
        description="Bạn sẽ cần đăng nhập lại để tiếp tục theo dõi habit."
        confirmLabel="Đăng xuất"
        destructive
        onCancel={() => setConfirmLogout(false)}
        onConfirm={() => {
          setConfirmLogout(false);
          logout();
        }}
      />
    </div>
  );
}

function SettingToggle({
  icon,
  label,
  desc,
  checked,
  onChange,
}: {
  icon: React.ReactNode;
  label: string;
  desc: string;
  checked: boolean;
  onChange: (v: boolean) => void;
}) {
  return (
    <label className="flex cursor-pointer items-center justify-between gap-3 rounded-xl p-2">
      <span className="flex items-center gap-3">
        {icon}
        <span>
          <span className="block text-sm font-medium text-foreground">{label}</span>
          <span className="block text-xs text-muted">{desc}</span>
        </span>
      </span>
      <button
        type="button"
        role="switch"
        aria-checked={checked}
        aria-label={label}
        onClick={() => onChange(!checked)}
        className={cn('focus-ring relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors', checked ? 'bg-primary' : 'bg-surface-2 ring-1 ring-border')}
      >
        <span className={cn('inline-block h-5 w-5 rounded-full bg-white shadow transition-transform', checked ? 'translate-x-5' : 'translate-x-0.5')} />
      </button>
    </label>
  );
}
