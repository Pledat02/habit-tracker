import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { habitsApi, checkinsApi } from '@/lib/resources';
import type { Checkin, Habit } from '@/lib/types';
import { toDateKey } from '@/lib/utils';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/context/ToastContext';

export function useHabits() {
  const { user } = useAuth();
  return useQuery({
    queryKey: ['habits', user?.id],
    queryFn: () => habitsApi.list(user!.id),
    enabled: !!user,
  });
}

export function useHabit(id: string | undefined) {
  return useQuery({
    queryKey: ['habit', id],
    queryFn: () => habitsApi.get(id!),
    enabled: !!id,
  });
}

/** All check-ins (used for dashboard aggregates + heatmaps). */
export function useCheckins() {
  const { user } = useAuth();
  return useQuery({
    queryKey: ['checkins', user?.id],
    queryFn: () => checkinsApi.listAll(),
    enabled: !!user,
  });
}

export function useHabitCheckins(habitId: string | undefined) {
  return useQuery({
    queryKey: ['checkins', 'habit', habitId],
    queryFn: () => checkinsApi.listByHabit(habitId!),
    enabled: !!habitId,
  });
}

export function useCreateHabit() {
  const qc = useQueryClient();
  const { user } = useAuth();
  const toast = useToast();
  return useMutation({
    mutationFn: (data: Omit<Habit, 'id' | 'createdAt' | 'userId'>) =>
      habitsApi.create({ ...data, userId: user!.id }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['habits'] });
      toast.success('Đã tạo habit mới');
    },
    onError: () => toast.error('Không tạo được habit, thử lại nhé'),
  });
}

export function useUpdateHabit() {
  const qc = useQueryClient();
  const toast = useToast();
  return useMutation({
    mutationFn: ({ id, patch }: { id: string; patch: Partial<Habit> }) => habitsApi.update(id, patch),
    onSuccess: (_data, vars) => {
      qc.invalidateQueries({ queryKey: ['habits'] });
      qc.invalidateQueries({ queryKey: ['habit', vars.id] });
      toast.success('Đã lưu thay đổi');
    },
    onError: () => toast.error('Lưu thất bại'),
  });
}

export function useDeleteHabit() {
  const qc = useQueryClient();
  const toast = useToast();
  return useMutation({
    mutationFn: (id: string) => habitsApi.remove(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['habits'] });
      qc.invalidateQueries({ queryKey: ['checkins'] });
      toast.success('Đã xóa habit');
    },
    onError: () => toast.error('Xóa thất bại'),
  });
}

/** Toggle today's check-in for a habit. */
export function useToggleCheckin() {
  const qc = useQueryClient();
  const toast = useToast();
  return useMutation({
    mutationFn: async ({
      habit,
      checkins,
      note = '',
      date = toDateKey(),
    }: {
      habit: Habit;
      checkins: Checkin[];
      note?: string;
      date?: string;
    }) => {
      const existing = checkins.find((c) => c.habitId === habit.id && c.date === date);
      if (existing) {
        await checkinsApi.remove(existing.id);
        return { action: 'removed' as const };
      }
      await checkinsApi.create({ habitId: habit.id, date, note });
      return { action: 'added' as const };
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['checkins'] });
    },
    onError: () => toast.error('Không cập nhật được check-in'),
  });
}
