import { useRef } from 'react';
import { Check, Loader2, PauseCircle } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { Checkin, Habit } from '@/lib/types';
import { Icon } from './ui/Icon';
import { StreakBadge } from './StreakBadge';
import { currentStreak, frequencyLabel, milestoneFor, toDateKey, cn } from '@/lib/utils';
import { burstConfetti } from '@/lib/confetti';

interface HabitCardProps {
  habit: Habit;
  checkins: Checkin[];
  onToggle: (habit: Habit) => void;
  pending?: boolean;
}

export function HabitCard({ habit, checkins, onToggle, pending = false }: HabitCardProps) {
  const btnRef = useRef<HTMLButtonElement>(null);
  const todayKey = toDateKey();
  const doneToday = checkins.some((c) => c.habitId === habit.id && c.date === todayKey);
  const streak = currentStreak(habit, checkins);
  const color = habit.color;

  const handleToggle = () => {
    // Fire confetti when completing (and especially on milestones) — before the
    // async round trip so it feels instant.
    if (!doneToday) {
      const rect = btnRef.current?.getBoundingClientRect();
      const origin = rect ? { x: rect.left + rect.width / 2, y: rect.top + rect.height / 2 } : undefined;
      const nextStreak = streak + 1;
      if (milestoneFor(nextStreak)) {
        burstConfetti(origin);
        setTimeout(() => burstConfetti(origin), 150);
      } else {
        burstConfetti(origin);
      }
    }
    onToggle(habit);
  };

  return (
    <div
      className={cn(
        'card flex items-center gap-3 p-4 transition-all hover:-translate-y-1 hover:shadow-soft-lg sm:gap-4',
        habit.paused && 'opacity-60',
      )}
    >
      <div className={cn('flex h-12 w-12 shrink-0 items-center justify-center rounded-xl', `bg-${color}/10`)}>
        <Icon name={habit.icon} className={cn('h-6 w-6', `text-${color}`)} aria-hidden />
      </div>

      <Link to={`/habits/${habit.id}`} className="min-w-0 flex-1 focus-ring rounded-lg">
        <div className="flex items-center gap-2">
          <h3 className="truncate font-semibold text-foreground">{habit.name}</h3>
          {habit.paused && <PauseCircle className="h-4 w-4 shrink-0 text-muted" aria-label="Tạm dừng" />}
        </div>
        <div className="mt-1 flex items-center gap-2 text-xs text-muted">
          <StreakBadge count={streak} atRisk={streak > 0 && !doneToday} />
          <span aria-hidden>·</span>
          <span className="truncate">{frequencyLabel(habit.frequency)}</span>
        </div>
      </Link>

      <button
        ref={btnRef}
        onClick={handleToggle}
        disabled={pending || habit.paused}
        aria-pressed={doneToday}
        aria-label={doneToday ? `Bỏ check-in ${habit.name}` : `Check-in ${habit.name} hôm nay`}
        className={cn(
          'focus-ring flex h-12 w-12 shrink-0 items-center justify-center rounded-full border-2 transition-all active:scale-90 disabled:cursor-not-allowed',
          doneToday
            ? `border-${color} bg-${color} text-white animate-pop-in`
            : 'border-border bg-surface text-muted hover:border-primary hover:text-primary',
        )}
      >
        {pending ? (
          <Loader2 className="h-5 w-5 animate-spin" aria-hidden />
        ) : (
          <Check className={cn('h-6 w-6 transition-transform', doneToday ? 'scale-100' : 'scale-90')} aria-hidden />
        )}
      </button>
    </div>
  );
}
