import { useMemo, useState } from 'react';
import { ListChecks, Plus } from 'lucide-react';
import { useHabits, useCheckins, useToggleCheckin } from '@/hooks/useHabits';
import { HabitCard } from '@/components/HabitCard';
import { HabitCardSkeleton } from '@/components/ui/Skeleton';
import { EmptyState } from '@/components/ui/EmptyState';
import { Button } from '@/components/ui/Button';
import { HabitFormModal } from '@/components/HabitFormModal';
import { useAchievementShare } from '@/context/AchievementContext';
import type { Habit } from '@/lib/types';
import { cn, currentStreak } from '@/lib/utils';

type Filter = 'all' | 'active' | 'paused';

export function HabitsList() {
  const { data: habits, isLoading } = useHabits();
  const { data: checkins } = useCheckins();
  const toggle = useToggleCheckin();
  const { celebrateStreak } = useAchievementShare();
  const [filter, setFilter] = useState<Filter>('all');
  const [createOpen, setCreateOpen] = useState(false);
  const [pendingId, setPendingId] = useState<string | null>(null);

  const filtered = useMemo(() => {
    const list = habits ?? [];
    if (filter === 'active') return list.filter((h) => !h.paused);
    if (filter === 'paused') return list.filter((h) => h.paused);
    return list;
  }, [habits, filter]);

  const handleToggle = async (habit: Habit) => {
    if (!checkins) return;
    const nextStreak = currentStreak(habit, checkins) + 1;
    setPendingId(habit.id);
    try {
      const res = await toggle.mutateAsync({ habit, checkins });
      if (res.action === 'added') await celebrateStreak(habit, nextStreak);
    } finally {
      setPendingId(null);
    }
  };

  const counts = {
    all: habits?.length ?? 0,
    active: habits?.filter((h) => !h.paused).length ?? 0,
    paused: habits?.filter((h) => h.paused).length ?? 0,
  };

  return (
    <div className="space-y-5">
      <header className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-foreground sm:text-3xl">Habits của bạn</h1>
        <Button onClick={() => setCreateOpen(true)} className="hidden sm:inline-flex">
          <Plus className="h-5 w-5" /> Tạo habit
        </Button>
      </header>

      <div className="flex gap-2">
        {(['all', 'active', 'paused'] as Filter[]).map((f) => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            className={cn(
              'focus-ring rounded-full px-3.5 py-1.5 text-sm font-medium transition-colors',
              filter === f ? 'bg-primary text-primary-foreground' : 'bg-surface-2 text-muted hover:text-foreground',
            )}
          >
            {f === 'all' ? 'Tất cả' : f === 'active' ? 'Đang chạy' : 'Tạm dừng'}
            <span className="ml-1.5 opacity-70">{counts[f]}</span>
          </button>
        ))}
      </div>

      <section className="space-y-3">
        {isLoading ? (
          <>
            <HabitCardSkeleton />
            <HabitCardSkeleton />
            <HabitCardSkeleton />
          </>
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={<ListChecks className="h-8 w-8" />}
            title={filter === 'all' ? 'Chưa có habit nào' : 'Không có habit ở mục này'}
            description={filter === 'all' ? 'Tạo habit đầu tiên để bắt đầu theo dõi.' : undefined}
            action={filter === 'all' ? <Button onClick={() => setCreateOpen(true)}>Tạo habit đầu tiên</Button> : undefined}
          />
        ) : (
          filtered.map((habit) => (
            <HabitCard
              key={habit.id}
              habit={habit}
              checkins={checkins ?? []}
              onToggle={handleToggle}
              pending={pendingId === habit.id}
            />
          ))
        )}
      </section>

      <HabitFormModal open={createOpen} onClose={() => setCreateOpen(false)} />
    </div>
  );
}
