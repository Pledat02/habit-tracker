import { useRef } from 'react';
import { motion, useReducedMotion } from 'motion/react';
import { Check, Loader2, PauseCircle } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { Checkin, Habit } from '@/lib/types';
import { Icon } from './ui/Icon';
import { StreakBadge } from './StreakBadge';
import { currentStreak, frequencyLabel, milestoneFor, streakUnit, toDateKey, cn } from '@/lib/utils';
import { burstConfetti } from '@/lib/confetti';

interface HabitCardProps {
  habit: Habit;
  checkins: Checkin[];
  onToggle: (habit: Habit) => void;
  pending?: boolean;
  /** Vị trí trong danh sách — tạo hiệu ứng xuất hiện lần lượt (stagger) thay vì cùng lúc. */
  index?: number;
}

export function HabitCard({ habit, checkins, onToggle, pending = false, index = 0 }: HabitCardProps) {
  const btnRef = useRef<HTMLButtonElement>(null);
  const reduceMotion = useReducedMotion();
  const todayKey = toDateKey();
  const doneToday = checkins.some((c) => c.habitId === habit.id && c.date === todayKey);
  const streak = currentStreak(habit, checkins);
  const unit = streakUnit(habit.frequency);
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
    <motion.div
      layout
      initial={reduceMotion ? { opacity: 0 } : { opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ type: 'spring', stiffness: 380, damping: 32, delay: reduceMotion ? 0 : index * 0.04 }}
      whileHover={reduceMotion ? undefined : { y: -4 }}
      className={cn(
        'card flex items-center gap-3 p-4 shadow-soft transition-shadow hover:shadow-soft-lg sm:gap-4',
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
          <StreakBadge count={streak} unit={unit} atRisk={unit === 'ngày' && streak > 0 && !doneToday} />
          <span aria-hidden>·</span>
          <span className="truncate">{frequencyLabel(habit.frequency)}</span>
        </div>
      </Link>

      <motion.button
        ref={btnRef}
        onClick={handleToggle}
        disabled={pending || habit.paused}
        aria-pressed={doneToday}
        aria-label={doneToday ? `Bỏ check-in ${habit.name}` : `Check-in ${habit.name} hôm nay`}
        whileTap={reduceMotion ? undefined : { scale: 0.88 }}
        animate={doneToday && !reduceMotion ? { scale: [1, 1.15, 1] } : { scale: 1 }}
        transition={{ type: 'spring', stiffness: 500, damping: 22 }}
        className={cn(
          'focus-ring flex h-12 w-12 shrink-0 items-center justify-center rounded-full border-2 transition-colors disabled:cursor-not-allowed',
          doneToday
            ? `border-${color} bg-${color} text-white`
            : 'border-border bg-surface text-muted hover:border-primary hover:text-primary',
        )}
      >
        {pending ? (
          <Loader2 className="h-5 w-5 animate-spin" aria-hidden />
        ) : (
          <Check className={cn('h-6 w-6 transition-transform', doneToday ? 'scale-100' : 'scale-90')} aria-hidden />
        )}
      </motion.button>
    </motion.div>
  );
}
