import * as Lucide from 'lucide-react';
import type { LucideProps } from 'lucide-react';

// Curated set for the habit icon picker (SVG icons, never emoji).
export const HABIT_ICONS = [
  'GlassWater',
  'BookOpen',
  'Dumbbell',
  'Brain',
  'Footprints',
  'Bed',
  'Apple',
  'Coffee',
  'Code2',
  'Languages',
  'PenLine',
  'Music',
  'Bike',
  'HeartPulse',
  'Leaf',
  'Sun',
  'Moon',
  'Wallet',
  'Smartphone',
  'Droplets',
  'Salad',
  'GraduationCap',
] as const;

export type HabitIconName = (typeof HABIT_ICONS)[number];

type IconComponent = React.ComponentType<LucideProps>;

/** Resolve a lucide icon by name, with a safe fallback. */
export function Icon({ name, ...props }: { name: string } & LucideProps) {
  const map = Lucide as unknown as Record<string, IconComponent>;
  const Cmp = map[name] ?? Lucide.Circle;
  return <Cmp {...props} />;
}
