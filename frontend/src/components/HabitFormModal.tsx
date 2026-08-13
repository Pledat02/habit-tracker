import { useEffect, useState } from 'react';
import { ChevronDown, Clock, Snowflake } from 'lucide-react';
import { Modal } from './ui/Modal';
import { Button } from './ui/Button';
import { Input } from './ui/Input';
import { Icon, HABIT_ICONS } from './ui/Icon';
import type { Habit } from '@/lib/types';
import { HABIT_COLORS, cn, encodeFrequency, parseFrequency } from '@/lib/utils';
import type { Frequency } from '@/lib/types';
import { useCreateHabit, useUpdateHabit } from '@/hooks/useHabits';

interface HabitFormModalProps {
  open: boolean;
  onClose: () => void;
  habit?: Habit; // edit mode when provided
  onCreated?: (id: string) => void;
}

type FreqPreset = 'daily' | 'weekly_3' | 'weekly_5' | 'days';
const DOW = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'];

const COLOR_LABEL: Record<string, string> = {
  primary: 'Teal',
  secondary: 'Indigo',
  success: 'Xanh lá',
  warning: 'Hổ phách',
  danger: 'Đỏ',
};

export function HabitFormModal({ open, onClose, habit, onCreated }: HabitFormModalProps) {
  const isEdit = !!habit;
  const createMut = useCreateHabit();
  const updateMut = useUpdateHabit();

  const [name, setName] = useState('');
  const [icon, setIcon] = useState<string>('GlassWater');
  const [color, setColor] = useState<string>('primary');
  const [preset, setPreset] = useState<FreqPreset>('daily');
  const [days, setDays] = useState<number[]>([1, 3, 5]);
  const [reminderTime, setReminderTime] = useState('');
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [streakFreeze, setStreakFreeze] = useState(false);
  const [paused, setPaused] = useState(false);
  const [error, setError] = useState('');

  // Hydrate form when opening (or when editing a different habit).
  useEffect(() => {
    if (!open) return;
    if (habit) {
      setName(habit.name);
      setIcon(habit.icon);
      setColor(habit.color);
      setReminderTime(habit.reminderTime);
      setPaused(!!habit.paused);
      const f = parseFrequency(habit.frequency);
      if (f === 'daily') setPreset('daily');
      else if (f === 'weekly_3') setPreset('weekly_3');
      else if (f === 'weekly_5') setPreset('weekly_5');
      else if (typeof f === 'object' && f.type === 'days') {
        setPreset('days');
        setDays(f.days);
      }
    } else {
      setName('');
      setIcon('GlassWater');
      setColor('primary');
      setPreset('daily');
      setDays([1, 3, 5]);
      setReminderTime('');
      setPaused(false);
      setStreakFreeze(false);
      setShowAdvanced(false);
    }
    setError('');
  }, [open, habit]);

  const buildFrequency = (): Frequency => {
    if (preset === 'daily') return 'daily';
    if (preset === 'weekly_3') return 'weekly_3';
    if (preset === 'weekly_5') return 'weekly_5';
    return { type: 'days', days: [...days].sort() };
  };

  const submit = async () => {
    if (!name.trim()) {
      setError('Vui lòng nhập tên habit');
      return;
    }
    if (preset === 'days' && days.length === 0) {
      setError('Chọn ít nhất 1 ngày trong tuần');
      return;
    }
    const payload = {
      name: name.trim(),
      icon,
      color,
      frequency: encodeFrequency(buildFrequency()),
      reminderTime,
      paused,
    };
    try {
      if (isEdit) {
        await updateMut.mutateAsync({ id: habit!.id, patch: payload });
      } else {
        const created = await createMut.mutateAsync(payload);
        onCreated?.(created.id);
      }
      onClose();
    } catch {
      setError('Có lỗi xảy ra, vui lòng thử lại');
    }
  };

  const toggleDay = (d: number) =>
    setDays((cur) => (cur.includes(d) ? cur.filter((x) => x !== d) : [...cur, d]));

  const loading = createMut.isPending || updateMut.isPending;

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={isEdit ? 'Chỉnh sửa habit' : 'Tạo habit mới'}
      footer={
        <div className="flex gap-3">
          <Button variant="outline" className="flex-1" onClick={onClose} disabled={loading}>
            Hủy
          </Button>
          <Button className="flex-1" onClick={submit} loading={loading}>
            {isEdit ? 'Lưu' : 'Tạo habit'}
          </Button>
        </div>
      }
    >
      <div className="space-y-5">
        <Input
          label="Tên habit"
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="VD: Uống 2L nước"
          error={error && !name.trim() ? error : undefined}
          autoFocus
        />

        {/* Icon picker */}
        <div>
          <span className="mb-1.5 block text-sm font-medium text-foreground">Icon</span>
          <div className="grid grid-cols-6 gap-2 sm:grid-cols-8">
            {HABIT_ICONS.map((n) => (
              <button
                key={n}
                type="button"
                onClick={() => setIcon(n)}
                aria-label={n}
                aria-pressed={icon === n}
                className={cn(
                  'focus-ring flex aspect-square items-center justify-center rounded-xl border transition-colors',
                  icon === n
                    ? `border-${color} bg-${color}/10 text-${color}`
                    : 'border-border text-muted hover:bg-surface-2',
                )}
              >
                <Icon name={n} className="h-5 w-5" />
              </button>
            ))}
          </div>
        </div>

        {/* Color */}
        <div>
          <span className="mb-1.5 block text-sm font-medium text-foreground">Màu</span>
          <div className="flex flex-wrap gap-2">
            {HABIT_COLORS.map((c) => (
              <button
                key={c}
                type="button"
                onClick={() => setColor(c)}
                aria-label={COLOR_LABEL[c]}
                aria-pressed={color === c}
                className={cn(
                  'focus-ring flex h-9 items-center gap-2 rounded-full border px-3 text-xs font-medium transition-all',
                  color === c ? 'border-foreground/30' : 'border-transparent',
                )}
              >
                <span className={cn('h-4 w-4 rounded-full', `bg-${c}`)} />
                {COLOR_LABEL[c]}
              </button>
            ))}
          </div>
        </div>

        {/* Frequency */}
        <div>
          <span className="mb-1.5 block text-sm font-medium text-foreground">Tần suất</span>
          <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
            {(
              [
                ['daily', 'Hàng ngày'],
                ['weekly_3', '3 lần/tuần'],
                ['weekly_5', '5 lần/tuần'],
                ['days', 'Ngày cụ thể'],
              ] as [FreqPreset, string][]
            ).map(([val, label]) => (
              <button
                key={val}
                type="button"
                onClick={() => setPreset(val)}
                aria-pressed={preset === val}
                className={cn(
                  'focus-ring rounded-xl border px-3 py-2.5 text-sm font-medium transition-colors',
                  preset === val
                    ? 'border-primary bg-primary/10 text-primary'
                    : 'border-border text-muted hover:bg-surface-2',
                )}
              >
                {label}
              </button>
            ))}
          </div>
          {preset === 'days' && (
            <div className="mt-3 flex flex-wrap gap-2">
              {DOW.map((label, d) => (
                <button
                  key={d}
                  type="button"
                  onClick={() => toggleDay(d)}
                  aria-pressed={days.includes(d)}
                  className={cn(
                    'focus-ring h-10 w-10 rounded-full border text-xs font-semibold transition-colors',
                    days.includes(d)
                      ? 'border-primary bg-primary text-primary-foreground'
                      : 'border-border text-muted hover:bg-surface-2',
                  )}
                >
                  {label}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Reminder time */}
        <Input
          label="Giờ nhắc nhở (không bắt buộc)"
          type="time"
          value={reminderTime}
          onChange={(e) => setReminderTime(e.target.value)}
          leftIcon={<Clock className="h-4 w-4" />}
        />

        {/* Progressive disclosure: advanced options */}
        <div>
          <button
            type="button"
            onClick={() => setShowAdvanced((s) => !s)}
            className="focus-ring flex w-full items-center justify-between rounded-xl px-1 py-2 text-sm font-medium text-muted hover:text-foreground"
          >
            Tùy chọn thêm
            <ChevronDown className={cn('h-4 w-4 transition-transform', showAdvanced && 'rotate-180')} />
          </button>
          {showAdvanced && (
            <div className="mt-2 space-y-3 rounded-xl bg-surface-2 p-3.5 animate-fade-in">
              <label className="flex cursor-pointer items-start justify-between gap-3">
                <span className="flex items-start gap-2.5">
                  <Snowflake className="mt-0.5 h-4 w-4 text-secondary" />
                  <span>
                    <span className="block text-sm font-medium text-foreground">Streak freeze</span>
                    <span className="block text-xs text-muted">Giữ streak khi lỡ 1 ngày (1 lần/tuần)</span>
                  </span>
                </span>
                <Toggle checked={streakFreeze} onChange={setStreakFreeze} />
              </label>
              {isEdit && (
                <label className="flex cursor-pointer items-center justify-between gap-3 border-t border-border pt-3">
                  <span className="text-sm font-medium text-foreground">Tạm dừng habit</span>
                  <Toggle checked={paused} onChange={setPaused} />
                </label>
              )}
            </div>
          )}
        </div>
      </div>
    </Modal>
  );
}

function Toggle({ checked, onChange }: { checked: boolean; onChange: (v: boolean) => void }) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      onClick={() => onChange(!checked)}
      className={cn(
        'focus-ring relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors',
        checked ? 'bg-primary' : 'bg-surface-2 ring-1 ring-border',
      )}
    >
      <span
        className={cn(
          'inline-block h-5 w-5 transform rounded-full bg-white shadow transition-transform',
          checked ? 'translate-x-5' : 'translate-x-0.5',
        )}
      />
    </button>
  );
}
