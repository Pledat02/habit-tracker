import { useMemo } from 'react';
import { Lock, Share2, Trophy } from 'lucide-react';
import { useHabits } from '@/hooks/useHabits';
import { useAchievements } from '@/hooks/useAchievements';
import { useAchievementShare } from '@/context/AchievementContext';
import { Icon } from '@/components/ui/Icon';
import { Skeleton } from '@/components/ui/Skeleton';
import { EmptyState } from '@/components/ui/EmptyState';
import { MILESTONES, MILESTONE_META, type MilestoneValue } from '@/lib/achievements';
import type { Achievement, Habit } from '@/lib/types';
import { cn } from '@/lib/utils';

export function AchievementsPage() {
  const { data: habits, isLoading } = useHabits();
  const { data: achievements } = useAchievements();
  const { reshareAchievement } = useAchievementShare();

  const unlockedCount = achievements?.length ?? 0;
  const totalPossible = (habits?.length ?? 0) * MILESTONES.length;

  // Map habitId -> milestone -> achievement
  const byHabit = useMemo(() => {
    const map = new Map<string, Map<number, Achievement>>();
    for (const a of achievements ?? []) {
      if (!map.has(a.habitId)) map.set(a.habitId, new Map());
      map.get(a.habitId)!.set(Number(a.milestone), a);
    }
    return map;
  }, [achievements]);

  if (isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-8 w-48" />
        <Skeleton className="h-24 rounded-2xl" />
        <Skeleton className="h-40 rounded-2xl" />
      </div>
    );
  }

  if ((habits ?? []).length === 0) {
    return (
      <div className="space-y-5">
        <h1 className="text-2xl font-bold text-foreground sm:text-3xl">Thành tựu của tôi</h1>
        <EmptyState
          icon={<Trophy className="h-8 w-8" />}
          title="Chưa có thành tựu nào"
          description="Tạo habit và duy trì streak để mở khóa các cột mốc 7 / 30 / 100 / 365 ngày."
        />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <header className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-foreground sm:text-3xl">Thành tựu của tôi</h1>
      </header>

      <section className="card flex items-center gap-4 p-5">
        <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-primary/10 text-primary">
          <Trophy className="h-7 w-7" />
        </div>
        <div>
          <p className="text-2xl font-bold text-foreground">
            {unlockedCount}
            <span className="text-base font-medium text-muted"> / {totalPossible} huy hiệu</span>
          </p>
          <p className="text-sm text-muted">Mỗi habit có 4 cột mốc: 7, 30, 100 và 365 ngày.</p>
        </div>
      </section>

      <div className="space-y-6">
        {(habits ?? []).map((habit) => (
          <HabitBadges
            key={habit.id}
            habit={habit}
            unlocked={byHabit.get(habit.id)}
            onShare={(a) => reshareAchievement(a, habit)}
          />
        ))}
      </div>
    </div>
  );
}

function HabitBadges({
  habit,
  unlocked,
  onShare,
}: {
  habit: Habit;
  unlocked: Map<number, Achievement> | undefined;
  onShare: (a: Achievement) => void;
}) {
  return (
    <section>
      <div className="mb-3 flex items-center gap-2.5">
        <span className={cn('flex h-8 w-8 items-center justify-center rounded-lg', `bg-${habit.color}/10`, `text-${habit.color}`)}>
          <Icon name={habit.icon} className="h-4.5 w-4.5" />
        </span>
        <h2 className="font-semibold text-foreground">{habit.name}</h2>
      </div>
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        {MILESTONES.map((m) => {
          const ach = unlocked?.get(m);
          return <Badge key={m} milestone={m} achievement={ach} onShare={onShare} />;
        })}
      </div>
    </section>
  );
}

function Badge({
  milestone,
  achievement,
  onShare,
}: {
  milestone: MilestoneValue;
  achievement: Achievement | undefined;
  onShare: (a: Achievement) => void;
}) {
  const meta = MILESTONE_META[milestone];
  const isUnlocked = !!achievement;

  if (!isUnlocked) {
    return (
      <div className="card flex flex-col items-center gap-2 p-4 text-center opacity-60">
        <div className="relative flex h-16 w-16 items-center justify-center rounded-full bg-surface-2">
          <Icon name={meta.icon} className="h-7 w-7 text-muted/50" />
          <span className="absolute -bottom-1 -right-1 flex h-6 w-6 items-center justify-center rounded-full border-2 border-surface bg-surface-2">
            <Lock className="h-3 w-3 text-muted" />
          </span>
        </div>
        <p className="text-sm font-semibold text-muted">{meta.label}</p>
        <p className="text-xs text-muted">Chưa mở khóa</p>
      </div>
    );
  }

  return (
    <button
      onClick={() => onShare(achievement)}
      className="card group flex flex-col items-center gap-2 p-4 text-center transition-shadow hover:shadow-soft-lg focus-ring"
    >
      <div
        className="flex h-16 w-16 items-center justify-center rounded-full text-white shadow-soft"
        style={{ background: 'linear-gradient(160deg, #14b8a6, #0f766e)' }}
      >
        <Icon name={meta.icon} className="h-8 w-8" strokeWidth={1.75} />
      </div>
      <p className="text-sm font-semibold text-foreground">{meta.label}</p>
      <span className="inline-flex items-center gap-1 text-xs font-medium text-primary opacity-0 transition-opacity group-hover:opacity-100">
        <Share2 className="h-3 w-3" /> Chia sẻ lại
      </span>
    </button>
  );
}
