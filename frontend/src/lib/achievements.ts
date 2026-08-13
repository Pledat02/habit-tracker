import type { Habit, Checkin } from './types';
import { currentStreak } from './utils';

export const MILESTONES = [7, 30, 100, 365] as const;
export type MilestoneValue = (typeof MILESTONES)[number];

export interface MilestoneMeta {
  milestone: MilestoneValue;
  /** Lucide icon name — distinct per tier. */
  icon: string;
  label: string;
  tagline: string;
}

// 7 = tia lửa nhỏ · 30 = lửa lớn · 100 = huy chương · 365 = cúp
export const MILESTONE_META: Record<MilestoneValue, MilestoneMeta> = {
  7: { milestone: 7, icon: 'Flame', label: '7 ngày', tagline: 'Khởi đầu rực lửa!' },
  30: { milestone: 30, icon: 'Flame', label: '30 ngày', tagline: 'Một tháng bền bỉ!' },
  100: { milestone: 100, icon: 'Medal', label: '100 ngày', tagline: 'Bậc thầy kiên trì!' },
  365: { milestone: 365, icon: 'Trophy', label: '365 ngày', tagline: 'Huyền thoại một năm!' },
};

export function isMilestone(streak: number): streak is MilestoneValue {
  return (MILESTONES as readonly number[]).includes(streak);
}

/** Highest milestone reached at or below the given streak (for manual share). */
export function highestMilestoneReached(streak: number): MilestoneValue | null {
  let best: MilestoneValue | null = null;
  for (const m of MILESTONES) if (streak >= m) best = m;
  return best;
}

/** Streak a habit will have if the user checks in today (today assumed not done). */
export function nextStreakIfCheckedInToday(habit: Habit, checkins: Checkin[]): number {
  return currentStreak(habit, checkins) + 1;
}
