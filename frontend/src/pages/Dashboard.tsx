import { useMemo, useState } from 'react';
import { Sparkles, Target } from 'lucide-react';
import { useHabits, useCheckins, useToggleCheckin } from '@/hooks/useHabits';
import { HabitCard } from '@/components/HabitCard';
import { HabitCardSkeleton } from '@/components/ui/Skeleton';
import { EmptyState } from '@/components/ui/EmptyState';
import { ProgressRing } from '@/components/ui/ProgressRing';
import { Button } from '@/components/ui/Button';
import { HabitFormModal } from '@/components/HabitFormModal';
import { useAuth } from '@/context/AuthContext';
import { useAchievementShare } from '@/context/AchievementContext';
import type { Habit } from '@/lib/types';
import { isScheduledOn, toDateKey } from '@/lib/utils';

export function Dashboard() {
  const { user } = useAuth();
  const { data: habits, isLoading: habitsLoading } = useHabits();
  const { data: checkins, isLoading: checkinsLoading } = useCheckins();
  const toggle = useToggleCheckin();
  const { celebrateNewAchievements } = useAchievementShare();
  const [createOpen, setCreateOpen] = useState(false);
  const [pendingId, setPendingId] = useState<string | null>(null);

  const today = new Date();
  const todayKey = toDateKey(today);

  const todays = useMemo(
    () => (habits ?? []).filter((h) => !h.paused && isScheduledOn(h, today)),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [habits],
  );

  const doneCount = useMemo(() => {
    if (!checkins) return 0;
    return todays.filter((h) => checkins.some((c) => c.habitId === h.id && c.date === todayKey)).length;
  }, [todays, checkins, todayKey]);

  const pct = todays.length ? (doneCount / todays.length) * 100 : 0;

  const handleToggle = async (habit: Habit) => {
    if (!checkins) return;
    setPendingId(habit.id);
    try {
      const res = await toggle.mutateAsync({ habit, checkins });
      if (res.action === 'added') celebrateNewAchievements(res.newAchievements, habit);
    } finally {
      setPendingId(null);
    }
  };

  const dateLabel = today.toLocaleDateString('vi-VN', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
  });

  const isLoading = habitsLoading || checkinsLoading;

  return (
    <div className="space-y-6">
      <header className="flex flex-col gap-1">
        <p className="text-sm capitalize text-muted">{dateLabel}</p>
        <h1 className="text-2xl font-bold text-foreground sm:text-3xl">
          Chào {user?.name?.split(' ').slice(-1)[0] || 'bạn'} 👋
        </h1>
      </header>

      {/* Overview */}
      {isLoading ? (
        <div className="card h-36 animate-pulse" />
      ) : todays.length > 0 ? (
        <section className="card flex items-center gap-5 p-5 sm:p-6">
          <ProgressRing value={pct} size={104} strokeWidth={9} />
          <div className="min-w-0">
            <div className="flex items-center gap-2 text-primary">
              <Sparkles className="h-4 w-4" />
              <span className="text-sm font-medium">Tiến độ hôm nay</span>
            </div>
            <p className="mt-1 text-2xl font-bold text-foreground">
              {doneCount}/{todays.length} habit
            </p>
            <p className="mt-1 text-sm text-muted">
              {pct === 100
                ? 'Tuyệt vời! Bạn đã hoàn thành tất cả hôm nay 🎉'
                : `Còn ${todays.length - doneCount} habit nữa là trọn vẹn ngày hôm nay.`}
            </p>
          </div>
        </section>
      ) : null}

      {/* Today's habits */}
      <section className="space-y-3">
        {isLoading ? (
          <>
            <HabitCardSkeleton />
            <HabitCardSkeleton />
            <HabitCardSkeleton />
          </>
        ) : (habits ?? []).length === 0 ? (
          <EmptyState
            icon={<Target className="h-8 w-8" />}
            title="Chưa có habit nào"
            description="Bắt đầu hành trình xây dựng thói quen tốt của bạn ngay hôm nay."
            action={<Button onClick={() => setCreateOpen(true)}>Tạo habit đầu tiên</Button>}
          />
        ) : todays.length === 0 ? (
          <EmptyState
            icon={<Sparkles className="h-8 w-8" />}
            title="Hôm nay không có habit nào theo lịch"
            description="Tận hưởng ngày nghỉ, hoặc thêm một habit mới cho hôm nay."
          />
        ) : (
          todays.map((habit, index) => (
            <HabitCard
              key={habit.id}
              habit={habit}
              checkins={checkins ?? []}
              onToggle={handleToggle}
              pending={pendingId === habit.id}
              index={index}
            />
          ))
        )}
      </section>

      <HabitFormModal open={createOpen} onClose={() => setCreateOpen(false)} />
    </div>
  );
}
