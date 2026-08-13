import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import type { Achievement, Habit } from '@/lib/types';
import { achievementsApi } from '@/lib/resources';
import { useAuth } from './AuthContext';
import {
  MILESTONE_META,
  highestMilestoneReached,
  isMilestone,
  type MilestoneValue,
} from '@/lib/achievements';
import type { ShareCardData } from '@/components/ShareCard';
import { AchievementModal, type SharePayload } from '@/components/AchievementModal';

interface AchievementCtx {
  /** Auto-trigger after a successful check-in. Shows congrats modal only for a
   *  newly reached (not previously recorded) milestone. */
  celebrateStreak: (habit: Habit, newStreak: number) => Promise<void>;
  /** Manual share of a habit's current streak (from Habit Detail). */
  shareHabitStreak: (habit: Habit, streak: number) => Promise<void>;
  /** Manual share of an aggregate recap (from Insights). */
  shareRecap: (data: ShareCardData, shareText: string) => void;
  /** Re-share an already unlocked achievement (from Achievements page). */
  reshareAchievement: (achievement: Achievement, habit: Habit | undefined) => void;
}

const Ctx = createContext<AchievementCtx | null>(null);

export function AchievementProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);
  const [payload, setPayload] = useState<SharePayload | null>(null);
  const userName = user?.name ?? 'Bạn';

  const buildStreakData = useCallback(
    (habit: Habit, streak: number): ShareCardData => {
      const m = highestMilestoneReached(streak);
      const meta = m ? MILESTONE_META[m] : null;
      return {
        title: habit.name,
        bigValue: String(streak),
        bigLabel: 'ngày streak',
        caption: meta ? meta.tagline : `Chuỗi ${streak} ngày liên tục!`,
        iconName: meta ? meta.icon : 'Flame',
        habitIconName: habit.icon,
        userName,
      };
    },
    [userName],
  );

  /** Find or create the achievement record for a habit+milestone.
   *  `isNew` distinguishes a freshly unlocked milestone from a previously
   *  recorded one (so we don't re-open the congrats modal on reload). */
  const ensureAchievement = useCallback(
    async (
      habit: Habit,
      milestone: MilestoneValue,
    ): Promise<{ record: Achievement; isNew: boolean } | null> => {
      if (!user) return null;
      const existing = await achievementsApi.list(user.id);
      const found = existing.find(
        (a) => a.habitId === habit.id && Number(a.milestone) === milestone && a.type === 'streak',
      );
      if (found) return { record: found, isNew: false };
      const created = await achievementsApi.create({
        userId: user.id,
        habitId: habit.id,
        type: 'streak',
        milestone,
        unlockedAt: new Date().toISOString(),
        shared: false,
      });
      qc.invalidateQueries({ queryKey: ['achievements'] });
      return { record: created, isNew: true };
    },
    [user, qc],
  );

  const celebrateStreak = useCallback(
    async (habit: Habit, newStreak: number) => {
      if (!isMilestone(newStreak) || !user) return;
      let result: { record: Achievement; isNew: boolean } | null = null;
      try {
        result = await ensureAchievement(habit, newStreak);
      } catch {
        // Achievement API failed — still congratulate, just can't persist/stats.
        result = null;
      }
      // Only show the modal if this milestone was newly unlocked (avoid re-show on reload).
      if (result && !result.isNew) return;
      const meta = MILESTONE_META[newStreak];
      setPayload({
        data: buildStreakData(habit, newStreak),
        modalTitle: `Chúc mừng! Mốc ${meta.label}`,
        celebrate: true,
        shareTitle: `Streak ${newStreak} ngày — ${habit.name}`,
        shareText: `Mình vừa đạt chuỗi ${newStreak} ngày "${habit.name}" trên Habit Tracker! 🔥`,
        achievementId: result?.record.id,
      });
      setOpen(true);
    },
    [user, ensureAchievement, buildStreakData],
  );

  const shareHabitStreak = useCallback(
    async (habit: Habit, streak: number) => {
      let achievementId: string | undefined;
      // If the current streak is (or passed) a milestone, make sure it's recorded.
      const reached = highestMilestoneReached(streak);
      if (reached) {
        try {
          const res = await ensureAchievement(habit, reached);
          achievementId = res?.record.id;
        } catch {
          /* ignore */
        }
      }
      setPayload({
        data: buildStreakData(habit, streak),
        modalTitle: 'Chia sẻ thành tựu',
        celebrate: false,
        shareTitle: `Streak ${streak} ngày — ${habit.name}`,
        shareText: `Chuỗi ${streak} ngày "${habit.name}" trên Habit Tracker! 🔥`,
        achievementId,
      });
      setOpen(true);
    },
    [ensureAchievement, buildStreakData],
  );

  const shareRecap = useCallback((data: ShareCardData, shareText: string) => {
    setPayload({
      data: { ...data, userName: data.userName },
      modalTitle: 'Chia sẻ tổng kết',
      celebrate: false,
      shareTitle: 'Tổng kết Habit Tracker',
      shareText,
    });
    setOpen(true);
  }, []);

  const reshareAchievement = useCallback(
    (achievement: Achievement, habit: Habit | undefined) => {
      const m = achievement.milestone as MilestoneValue;
      const meta = MILESTONE_META[m] ?? MILESTONE_META[7];
      const data: ShareCardData = {
        title: habit?.name ?? 'Thói quen',
        bigValue: String(achievement.milestone),
        bigLabel: 'ngày streak',
        caption: meta.tagline,
        iconName: meta.icon,
        habitIconName: habit?.icon,
        userName,
      };
      setPayload({
        data,
        modalTitle: 'Chia sẻ thành tựu',
        celebrate: false,
        shareTitle: `Mốc ${meta.label}`,
        shareText: `Mình đã đạt mốc ${meta.label} trên Habit Tracker! 🔥`,
        achievementId: achievement.id,
      });
      setOpen(true);
    },
    [userName],
  );

  const value = useMemo<AchievementCtx>(
    () => ({ celebrateStreak, shareHabitStreak, shareRecap, reshareAchievement }),
    [celebrateStreak, shareHabitStreak, shareRecap, reshareAchievement],
  );

  return (
    <Ctx.Provider value={value}>
      {children}
      <AchievementModal open={open} payload={payload} onClose={() => setOpen(false)} />
    </Ctx.Provider>
  );
}

export function useAchievementShare(): AchievementCtx {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error('useAchievementShare must be used within AchievementProvider');
  return ctx;
}
