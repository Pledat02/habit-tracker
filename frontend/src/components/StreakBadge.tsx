import { Flame } from 'lucide-react';
import { cn } from '@/lib/utils';

interface StreakBadgeProps {
  count: number;
  atRisk?: boolean; // streak about to be lost (not done today yet)
  size?: 'sm' | 'md';
  /** 'ngày' cho habit hàng ngày / ngày cụ thể, 'tuần' cho habit "N lần/tuần". */
  unit?: 'ngày' | 'tuần';
}

/** Streak indicator using an SVG flame icon (never emoji). */
export function StreakBadge({ count, atRisk = false, size = 'sm', unit = 'ngày' }: StreakBadgeProps) {
  if (count <= 0) {
    return (
      <span className={cn('inline-flex items-center gap-1 text-muted', size === 'sm' ? 'text-xs' : 'text-sm')}>
        <Flame className={size === 'sm' ? 'h-3.5 w-3.5' : 'h-4 w-4'} aria-hidden />
        Chưa có streak
      </span>
    );
  }

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1 rounded-full px-2 py-0.5 font-semibold',
        atRisk ? 'bg-warning/15 text-warning' : 'bg-primary/10 text-primary',
        size === 'sm' ? 'text-xs' : 'text-sm',
      )}
      title={atRisk ? 'Chưa check-in hôm nay — đừng để mất streak!' : `Chuỗi ${count} ${unit}`}
    >
      <Flame className={size === 'sm' ? 'h-3.5 w-3.5' : 'h-4 w-4'} aria-hidden />
      {count} {unit}
    </span>
  );
}
