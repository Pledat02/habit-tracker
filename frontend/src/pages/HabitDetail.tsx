import { useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  BarChart,
  Bar,
  ResponsiveContainer,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from 'recharts';
import {
  ArrowLeft,
  Check,
  Flame,
  Pencil,
  Trophy,
  Trash2,
  Pause,
  Play,
  Share2,
  StickyNote,
} from 'lucide-react';
import { useHabit, useHabitCheckins, useDeleteHabit, useUpdateHabit, useToggleCheckin } from '@/hooks/useHabits';
import { Icon } from '@/components/ui/Icon';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { CalendarHeatmap } from '@/components/CalendarHeatmap';
import { HabitFormModal } from '@/components/HabitFormModal';
import { ConfirmDialog } from '@/components/ui/ConfirmDialog';
import { Skeleton } from '@/components/ui/Skeleton';
import { useAchievementShare } from '@/context/AchievementContext';
import { burstConfetti } from '@/lib/confetti';
import {
  addDays,
  cn,
  currentStreak,
  frequencyLabel,
  longestStreak,
  milestoneFor,
  toDateKey,
} from '@/lib/utils';

export function HabitDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { data: habit, isLoading } = useHabit(id);
  const { data: checkins } = useHabitCheckins(id);
  const del = useDeleteHabit();
  const update = useUpdateHabit();
  const toggle = useToggleCheckin();
  const { celebrateStreak, shareHabitStreak } = useAchievementShare();

  const [editOpen, setEditOpen] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [note, setNote] = useState('');
  const [toggling, setToggling] = useState(false);

  const doneSet = useMemo(() => new Set((checkins ?? []).map((c) => c.date)), [checkins]);
  const todayKey = toDateKey();
  const doneToday = doneSet.has(todayKey);

  const stats = useMemo(() => {
    if (!habit || !checkins) return { cur: 0, best: 0, total: 0, rate: 0 };
    const cur = currentStreak(habit, checkins);
    const best = Math.max(longestStreak(habit, checkins), cur);
    const total = checkins.length;
    // completion rate over last 30 days
    const start = addDays(new Date(), -29);
    let scheduled = 0;
    let done = 0;
    for (let i = 0; i < 30; i++) {
      const d = addDays(start, i);
      scheduled++;
      if (doneSet.has(toDateKey(d))) done++;
    }
    return { cur, best, total, rate: scheduled ? Math.round((done / scheduled) * 100) : 0 };
  }, [habit, checkins, doneSet]);

  // Weekly completion for the last 8 weeks.
  const weekly = useMemo(() => {
    const out: { label: string; value: number }[] = [];
    const today = new Date();
    for (let w = 7; w >= 0; w--) {
      const end = addDays(today, -w * 7);
      let count = 0;
      for (let d = 0; d < 7; d++) {
        if (doneSet.has(toDateKey(addDays(end, -d)))) count++;
      }
      out.push({ label: w === 0 ? 'Tuần này' : `-${w}t`, value: count });
    }
    return out;
  }, [doneSet]);

  const handleToggle = async () => {
    if (!habit || !checkins) return;
    setToggling(true);
    if (!doneToday) {
      const next = stats.cur + 1;
      burstConfetti();
      if (milestoneFor(next)) setTimeout(burstConfetti, 150);
    }
    try {
      const res = await toggle.mutateAsync({ habit, checkins, note: doneToday ? '' : note });
      setNote('');
      if (res.action === 'added') await celebrateStreak(habit, stats.cur + 1);
    } finally {
      setToggling(false);
    }
  };

  if (isLoading || !habit) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-8 w-32" />
        <Skeleton className="h-28 w-full rounded-2xl" />
        <Skeleton className="h-40 w-full rounded-2xl" />
      </div>
    );
  }

  const color = habit.color;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <Link
          to="/habits"
          className="focus-ring inline-flex items-center gap-1.5 rounded-lg text-sm font-medium text-muted hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" /> Quay lại
        </Link>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={() => setEditOpen(true)}>
            <Pencil className="h-4 w-4" /> Sửa
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={() => update.mutate({ id: habit.id, patch: { paused: !habit.paused } })}
          >
            {habit.paused ? <Play className="h-4 w-4" /> : <Pause className="h-4 w-4" />}
            {habit.paused ? 'Tiếp tục' : 'Tạm dừng'}
          </Button>
          <Button variant="ghost" size="icon" aria-label="Xóa habit" onClick={() => setConfirmDelete(true)}>
            <Trash2 className="h-4 w-4 text-danger" />
          </Button>
        </div>
      </div>

      {/* Header card */}
      <section className="card flex flex-col gap-4 p-5 sm:flex-row sm:items-center sm:justify-between sm:p-6">
        <div className="flex items-center gap-4">
          <div className={cn('flex h-14 w-14 items-center justify-center rounded-2xl', `bg-${color}/10`)}>
            <Icon name={habit.icon} className={cn('h-7 w-7', `text-${color}`)} />
          </div>
          <div>
            <h1 className="text-xl font-bold text-foreground sm:text-2xl">{habit.name}</h1>
            <p className="text-sm text-muted">
              {frequencyLabel(habit.frequency)}
              {habit.reminderTime && ` · nhắc ${habit.reminderTime}`}
            </p>
          </div>
        </div>
        <button
          onClick={handleToggle}
          disabled={toggling || habit.paused}
          className={cn(
            'focus-ring inline-flex h-12 items-center justify-center gap-2 rounded-xl px-5 font-semibold transition-all active:scale-95 disabled:opacity-60',
            doneToday ? `bg-${color} text-white` : `border-2 border-${color} text-${color} hover:bg-${color}/10`,
          )}
        >
          <Check className="h-5 w-5" />
          {doneToday ? 'Đã xong hôm nay' : 'Check-in hôm nay'}
        </button>
      </section>

      {/* Stats */}
      <section className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <StatCard
          icon={<Flame className="h-5 w-5" />}
          label="Streak hiện tại"
          value={`${stats.cur} ngày`}
          accent="primary"
          action={
            stats.cur > 0 ? (
              <button
                onClick={() => shareHabitStreak(habit, stats.cur)}
                aria-label="Chia sẻ streak"
                className="focus-ring rounded-lg p-1.5 text-muted transition-colors hover:bg-surface-2 hover:text-primary"
              >
                <Share2 className="h-4 w-4" />
              </button>
            ) : undefined
          }
        />
        <StatCard icon={<Trophy className="h-5 w-5" />} label="Dài nhất" value={`${stats.best} ngày`} accent="warning" />
        <StatCard icon={<Check className="h-5 w-5" />} label="Tổng check-in" value={`${stats.total}`} accent="success" />
        <StatCard icon={<BarChartMini />} label="Tỷ lệ 30 ngày" value={`${stats.rate}%`} accent="secondary" />
      </section>

      {/* Quick note for today's check-in */}
      {!doneToday && !habit.paused && (
        <section className="card p-4">
          <Input
            label="Ghi chú nhanh (không bắt buộc)"
            placeholder="Cảm nhận hôm nay..."
            value={note}
            onChange={(e) => setNote(e.target.value)}
            leftIcon={<StickyNote className="h-4 w-4" />}
          />
          <p className="mt-2 text-xs text-muted">Ghi chú sẽ được lưu cùng lần check-in hôm nay.</p>
        </section>
      )}

      {/* Heatmap */}
      <section className="card p-5">
        <h2 className="mb-4 text-sm font-semibold text-foreground">Lịch sử hoàn thành</h2>
        <CalendarHeatmap done={doneSet} color={color} />
      </section>

      {/* Weekly chart */}
      <section className="card p-5">
        <h2 className="mb-4 text-sm font-semibold text-foreground">Số lần / tuần (8 tuần gần nhất)</h2>
        <div className="h-56 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={weekly} margin={{ top: 4, right: 4, left: -24, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgb(var(--color-border))" vertical={false} />
              <XAxis dataKey="label" tick={{ fontSize: 12, fill: 'rgb(var(--color-muted))' }} axisLine={false} tickLine={false} />
              <YAxis allowDecimals={false} tick={{ fontSize: 12, fill: 'rgb(var(--color-muted))' }} axisLine={false} tickLine={false} />
              <Tooltip
                cursor={{ fill: 'rgb(var(--color-surface-2))' }}
                contentStyle={{
                  background: 'rgb(var(--color-surface))',
                  border: '1px solid rgb(var(--color-border))',
                  borderRadius: 12,
                  fontSize: 12,
                  color: 'rgb(var(--color-foreground))',
                }}
                labelStyle={{ color: 'rgb(var(--color-muted))' }}
                formatter={(v: number) => [`${v} lần`, 'Hoàn thành']}
              />
              <Bar dataKey="value" fill={`rgb(var(--color-${color}))`} radius={[6, 6, 0, 0]} maxBarSize={40} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </section>

      <HabitFormModal open={editOpen} onClose={() => setEditOpen(false)} habit={habit} />
      <ConfirmDialog
        open={confirmDelete}
        title="Xóa habit này?"
        description={`"${habit.name}" và toàn bộ lịch sử check-in sẽ bị xóa vĩnh viễn. Hành động này không thể hoàn tác.`}
        confirmLabel="Xóa"
        destructive
        loading={del.isPending}
        onCancel={() => setConfirmDelete(false)}
        onConfirm={async () => {
          await del.mutateAsync(habit.id);
          navigate('/habits');
        }}
      />
    </div>
  );
}

function StatCard({
  icon,
  label,
  value,
  accent,
  action,
}: {
  icon: React.ReactNode;
  label: string;
  value: string;
  accent: string;
  action?: React.ReactNode;
}) {
  return (
    <div className="card p-4">
      <div className="mb-2 flex items-start justify-between">
        <div className={cn('inline-flex h-9 w-9 items-center justify-center rounded-lg', `bg-${accent}/10`, `text-${accent}`)}>
          {icon}
        </div>
        {action}
      </div>
      <p className="text-lg font-bold text-foreground">{value}</p>
      <p className="text-xs text-muted">{label}</p>
    </div>
  );
}

function BarChartMini() {
  return (
    <svg viewBox="0 0 20 20" className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
      <path d="M4 16V9M10 16V4M16 16v-4" />
    </svg>
  );
}
