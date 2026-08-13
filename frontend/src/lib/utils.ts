import type { Checkin, Frequency, Habit } from './types';

/** Tiny classnames helper. */
export function cn(...parts: Array<string | false | null | undefined>): string {
  return parts.filter(Boolean).join(' ');
}

export function uid(): string {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 8);
}

/** Local-date 'YYYY-MM-DD' (avoids UTC off-by-one from toISOString). */
export function toDateKey(d: Date = new Date()): string {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

export function parseDateKey(key: string): Date {
  const [y, m, d] = key.split('-').map(Number);
  return new Date(y, m - 1, d);
}

export function addDays(d: Date, n: number): Date {
  const c = new Date(d);
  c.setDate(c.getDate() + n);
  return c;
}

export function parseFrequency(raw: string): Frequency {
  try {
    const v = JSON.parse(raw);
    return v as Frequency;
  } catch {
    return 'daily';
  }
}

export function encodeFrequency(f: Frequency): string {
  return JSON.stringify(f);
}

export function frequencyLabel(raw: string): string {
  const f = parseFrequency(raw);
  if (f === 'daily') return 'Hàng ngày';
  if (f === 'weekly_3') return '3 lần / tuần';
  if (f === 'weekly_5') return '5 lần / tuần';
  if (typeof f === 'object' && f.type === 'days') {
    const names = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'];
    return f.days.map((d) => names[d]).join(', ');
  }
  return 'Hàng ngày';
}

/** Is this habit scheduled to be done on the given date? */
export function isScheduledOn(habit: Habit, date: Date): boolean {
  const f = parseFrequency(habit.frequency);
  if (f === 'daily' || f === 'weekly_3' || f === 'weekly_5') return true;
  if (typeof f === 'object' && f.type === 'days') return f.days.includes(date.getDay());
  return true;
}

/**
 * Current consecutive streak (in scheduled days) up to today.
 * Counts back from today while each scheduled day has a check-in.
 * Today not being done yet does not break the streak.
 */
export function currentStreak(habit: Habit, checkins: Checkin[]): number {
  const done = new Set(checkins.filter((c) => c.habitId === habit.id).map((c) => c.date));
  let streak = 0;
  const cursor = new Date();
  const todayKey = toDateKey();
  // Walk back up to ~2 years max as a safety bound.
  for (let i = 0; i < 730; i++) {
    const key = toDateKey(cursor);
    const scheduled = isScheduledOn(habit, cursor);
    if (scheduled) {
      if (done.has(key)) {
        streak++;
      } else if (key !== todayKey) {
        break; // a missed scheduled day in the past breaks the streak
      }
    }
    cursor.setDate(cursor.getDate() - 1);
  }
  return streak;
}

export function longestStreak(habit: Habit, checkins: Checkin[]): number {
  const dates = checkins
    .filter((c) => c.habitId === habit.id)
    .map((c) => c.date)
    .sort();
  if (dates.length === 0) return 0;
  let best = 1;
  let run = 1;
  for (let i = 1; i < dates.length; i++) {
    const prev = parseDateKey(dates[i - 1]);
    const cur = parseDateKey(dates[i]);
    const diff = Math.round((cur.getTime() - prev.getTime()) / 86400000);
    if (diff === 1) {
      run++;
      best = Math.max(best, run);
    } else if (diff > 1) {
      run = 1;
    }
  }
  return best;
}

/** Streak milestone reached exactly on this check-in? */
export function milestoneFor(streak: number): number | null {
  return [7, 30, 100, 365].includes(streak) ? streak : null;
}

export function initials(name: string): string {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase())
    .join('');
}

/** Map color token -> Tailwind text/bg utility fragments. */
export const HABIT_COLORS = ['primary', 'secondary', 'success', 'warning', 'danger'] as const;
export type HabitColor = (typeof HABIT_COLORS)[number];

export function colorClasses(color: string): { text: string; bg: string; ring: string; soft: string } {
  const c = (HABIT_COLORS as readonly string[]).includes(color) ? color : 'primary';
  return {
    text: `text-${c}`,
    bg: `bg-${c}`,
    ring: `ring-${c}`,
    soft: `bg-${c}/10 text-${c}`,
  };
}
