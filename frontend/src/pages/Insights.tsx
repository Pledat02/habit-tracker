import { useMemo } from 'react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from 'recharts';
import { TrendingUp, TrendingDown, Award, CalendarRange, Flame, Share2 } from 'lucide-react';
import { useHabits, useCheckins } from '@/hooks/useHabits';
import { Icon } from '@/components/ui/Icon';
import { Skeleton } from '@/components/ui/Skeleton';
import { EmptyState } from '@/components/ui/EmptyState';
import { Button } from '@/components/ui/Button';
import { useAuth } from '@/context/AuthContext';
import { useAchievementShare } from '@/context/AchievementContext';
import { addDays, cn, currentStreak, longestStreak, toDateKey } from '@/lib/utils';

export function Insights() {
  const { data: habits, isLoading } = useHabits();
  const { data: checkins } = useCheckins();
  const { user } = useAuth();
  const { shareRecap } = useAchievementShare();

  const trend = useMemo(() => {
    const set = new Set((checkins ?? []).map((c) => `${c.habitId}|${c.date}`));
    const active = (habits ?? []).filter((h) => !h.paused);
    const out: { label: string; value: number }[] = [];
    const today = new Date();
    for (let i = 29; i >= 0; i--) {
      const d = addDays(today, -i);
      const key = toDateKey(d);
      const done = active.filter((h) => set.has(`${h.id}|${key}`)).length;
      out.push({ label: `${d.getDate()}/${d.getMonth() + 1}`, value: done });
    }
    return out;
  }, [habits, checkins]);

  const ranking = useMemo(() => {
    if (!habits || !checkins) return { best: null as null | RankRow, worst: null as null | RankRow, rows: [] as RankRow[] };
    const rows: RankRow[] = habits
      .filter((h) => !h.paused)
      .map((h) => {
        const hc = checkins.filter((c) => c.habitId === h.id);
        const set = new Set(hc.map((c) => c.date));
        let done = 0;
        for (let i = 0; i < 30; i++) if (set.has(toDateKey(addDays(new Date(), -i)))) done++;
        return {
          id: h.id,
          name: h.name,
          icon: h.icon,
          color: h.color,
          rate: Math.round((done / 30) * 100),
          streak: currentStreak(h, checkins),
        };
      })
      .sort((a, b) => b.rate - a.rate);
    return { best: rows[0] ?? null, worst: rows[rows.length - 1] ?? null, rows };
  }, [habits, checkins]);

  const totals = useMemo(() => {
    const totalCheckins = checkins?.length ?? 0;
    const best = (habits ?? []).reduce((m, h) => Math.max(m, longestStreak(h, checkins ?? [])), 0);
    const activeDays = new Set((checkins ?? []).map((c) => c.date)).size;
    return { totalCheckins, best, activeDays };
  }, [habits, checkins]);

  if (isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-8 w-40" />
        <div className="grid grid-cols-3 gap-3">
          <Skeleton className="h-24 rounded-2xl" />
          <Skeleton className="h-24 rounded-2xl" />
          <Skeleton className="h-24 rounded-2xl" />
        </div>
        <Skeleton className="h-64 rounded-2xl" />
      </div>
    );
  }

  if ((habits ?? []).length === 0) {
    return (
      <div className="space-y-5">
        <h1 className="text-2xl font-bold text-foreground sm:text-3xl">Thống kê</h1>
        <EmptyState icon={<CalendarRange className="h-8 w-8" />} title="Chưa có dữ liệu" description="Tạo habit và check-in vài ngày để xem thống kê tại đây." />
      </div>
    );
  }

  const onShareRecap = () => {
    shareRecap(
      {
        title: 'Tổng kết của tôi',
        bigValue: String(totals.activeDays),
        bigLabel: 'ngày duy trì',
        caption: `${totals.totalCheckins} check-in · streak kỷ lục ${totals.best} ngày`,
        iconName: 'Award',
        userName: user?.name ?? 'Bạn',
      },
      `Tổng kết Habit Tracker của mình: ${totals.activeDays} ngày duy trì, ${totals.totalCheckins} check-in, streak kỷ lục ${totals.best} ngày! 💪`,
    );
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-3">
        <h1 className="text-2xl font-bold text-foreground sm:text-3xl">Thống kê</h1>
        <Button variant="outline" size="sm" onClick={onShareRecap}>
          <Share2 className="h-4 w-4" /> Chia sẻ recap
        </Button>
      </div>

      {/* Recap totals */}
      <section className="grid grid-cols-3 gap-3">
        <Recap icon={<CalendarRange className="h-5 w-5" />} label="Ngày duy trì" value={totals.activeDays} accent="primary" />
        <Recap icon={<Award className="h-5 w-5" />} label="Tổng check-in" value={totals.totalCheckins} accent="success" />
        <Recap icon={<Flame className="h-5 w-5" />} label="Streak kỷ lục" value={totals.best} accent="warning" />
      </section>

      {/* Best / worst */}
      <section className="grid gap-3 sm:grid-cols-2">
        {ranking.best && <HighlightCard title="Habit tốt nhất" row={ranking.best} up />}
        {ranking.worst && ranking.rows.length > 1 && <HighlightCard title="Cần cải thiện" row={ranking.worst} />}
      </section>

      {/* 30-day trend */}
      <section className="card p-5">
        <h2 className="mb-4 text-sm font-semibold text-foreground">Số habit hoàn thành / ngày (30 ngày)</h2>
        <div className="h-60 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={trend} margin={{ top: 4, right: 8, left: -24, bottom: 0 }}>
              <defs>
                <linearGradient id="colorValue" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="rgb(var(--color-primary))" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="rgb(var(--color-primary))" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="rgb(var(--color-border))" vertical={false} />
              <XAxis dataKey="label" tick={{ fontSize: 11, fill: 'rgb(var(--color-muted))' }} axisLine={false} tickLine={false} interval={4} />
              <YAxis allowDecimals={false} tick={{ fontSize: 11, fill: 'rgb(var(--color-muted))' }} axisLine={false} tickLine={false} />
              <Tooltip
                contentStyle={{
                  background: 'rgb(var(--color-surface))',
                  border: '1px solid rgb(var(--color-border))',
                  borderRadius: 12,
                  fontSize: 12,
                  color: 'rgb(var(--color-foreground))',
                  boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1)',
                }}
                formatter={(v: number) => [`${v} habit`, 'Hoàn thành']}
              />
              <Area type="monotone" dataKey="value" stroke="rgb(var(--color-primary))" fillOpacity={1} fill="url(#colorValue)" strokeWidth={2.5} activeDot={{ r: 5 }} />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </section>

      {/* Ranking list */}
      <section className="card divide-y divide-border">
        <h2 className="px-5 py-4 text-sm font-semibold text-foreground">Xếp hạng theo tỷ lệ (30 ngày)</h2>
        {ranking.rows.map((r) => (
          <div key={r.id} className="flex items-center gap-3 px-5 py-3">
            <div className={cn('flex h-9 w-9 items-center justify-center rounded-lg', `bg-${r.color}/10`, `text-${r.color}`)}>
              <Icon name={r.icon} className="h-4.5 w-4.5" />
            </div>
            <span className="flex-1 truncate text-sm font-medium text-foreground">{r.name}</span>
            <div className="h-2 w-24 overflow-hidden rounded-full bg-surface-2">
              <div className={cn('h-full rounded-full', `bg-${r.color}`)} style={{ width: `${r.rate}%` }} />
            </div>
            <span className="w-10 text-right text-sm font-semibold text-foreground">{r.rate}%</span>
          </div>
        ))}
      </section>
    </div>
  );
}

interface RankRow {
  id: string;
  name: string;
  icon: string;
  color: string;
  rate: number;
  streak: number;
}

function Recap({ icon, label, value, accent }: { icon: React.ReactNode; label: string; value: number; accent: string }) {
  return (
    <div className="card p-4 text-center sm:text-left">
      <div className={cn('mb-2 inline-flex h-9 w-9 items-center justify-center rounded-lg', `bg-${accent}/10`, `text-${accent}`)}>{icon}</div>
      <p className="text-xl font-bold text-foreground">{value}</p>
      <p className="text-xs text-muted">{label}</p>
    </div>
  );
}

function HighlightCard({ title, row, up = false }: { title: string; row: RankRow; up?: boolean }) {
  return (
    <div className="card flex items-center gap-4 p-5">
      <div className={cn('flex h-12 w-12 items-center justify-center rounded-xl', `bg-${row.color}/10`, `text-${row.color}`)}>
        <Icon name={row.icon} className="h-6 w-6" />
      </div>
      <div className="min-w-0 flex-1">
        <p className="flex items-center gap-1.5 text-xs font-medium text-muted">
          {up ? <TrendingUp className="h-3.5 w-3.5 text-success" /> : <TrendingDown className="h-3.5 w-3.5 text-warning" />}
          {title}
        </p>
        <p className="truncate font-semibold text-foreground">{row.name}</p>
      </div>
      <div className="text-right">
        <p className="text-lg font-bold text-foreground">{row.rate}%</p>
        <p className="text-xs text-muted">30 ngày</p>
      </div>
    </div>
  );
}
