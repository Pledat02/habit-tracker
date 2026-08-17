import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
import type { Achievement, Habit } from '@/lib/types';
import { useAuth } from './AuthContext';
import { MILESTONE_META, highestMilestoneReached, type MilestoneValue } from '@/lib/achievements';
import type { ShareCardData } from '@/components/ShareCard';
import { AchievementModal, type SharePayload } from '@/components/AchievementModal';

interface AchievementCtx {
  /** Sau check-in: hiện chúc mừng cho các thành tựu BACKEND vừa cấp (engine).
   *  Không còn tự tính/cấp ở client — chỉ hiển thị. */
  celebrateNewAchievements: (newAchievements: Achievement[], habit: Habit) => void;
  /** Manual share of a habit's current streak (from Habit Detail). */
  shareHabitStreak: (habit: Habit, streak: number) => void;
  /** Manual share of an aggregate recap (from Insights). */
  shareRecap: (data: ShareCardData, shareText: string) => void;
  /** Re-share an already unlocked achievement (from Achievements page). */
  reshareAchievement: (achievement: Achievement, habit: Habit | undefined) => void;
}

const Ctx = createContext<AchievementCtx | null>(null);

export function AchievementProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
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

  const celebrateNewAchievements = useCallback(
    (newAchievements: Achievement[], habit: Habit) => {
      // Chỉ hiện 1 modal cho cái "cao" nhất vừa mở khoá (nhiều mốc cùng lúc thì lấy mốc lớn nhất).
      const withMilestone = newAchievements.filter((a) => a.milestone != null);
      if (withMilestone.length === 0) return;
      const top = withMilestone.reduce((best, a) => (a.milestone! > best.milestone! ? a : best));
      const milestone = top.milestone as MilestoneValue;
      const meta = MILESTONE_META[milestone] ?? MILESTONE_META[7];
      setPayload({
        data: buildStreakData(habit, milestone),
        modalTitle: `Chúc mừng! Mốc ${meta.label}`,
        celebrate: true,
        shareTitle: `Streak ${milestone} ngày — ${habit.name}`,
        shareText: `Mình vừa đạt chuỗi ${milestone} ngày "${habit.name}" trên Habit Tracker! 🔥`,
        achievementId: top.id,
      });
      setOpen(true);
    },
    [buildStreakData],
  );

  const shareHabitStreak = useCallback(
    (habit: Habit, streak: number) => {
      setPayload({
        data: buildStreakData(habit, streak),
        modalTitle: 'Chia sẻ thành tựu',
        celebrate: false,
        shareTitle: `Streak ${streak} ngày — ${habit.name}`,
        shareText: `Chuỗi ${streak} ngày "${habit.name}" trên Habit Tracker! 🔥`,
      });
      setOpen(true);
    },
    [buildStreakData],
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
      const m = (achievement.milestone ?? 7) as MilestoneValue;
      const meta = MILESTONE_META[m] ?? MILESTONE_META[7];
      const data: ShareCardData = {
        title: habit?.name ?? 'Thói quen',
        bigValue: String(achievement.milestone ?? ''),
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
    () => ({ celebrateNewAchievements, shareHabitStreak, shareRecap, reshareAchievement }),
    [celebrateNewAchievements, shareHabitStreak, shareRecap, reshareAchievement],
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
