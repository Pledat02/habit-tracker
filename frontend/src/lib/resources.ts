import { api } from './apiClient';
import type { Achievement, Checkin, Habit, User } from './types';

// Thin resource layer over the generic client. Endpoint paths match a typical
// Spring Boot REST controller (/habits, /checkins) so nothing changes on swap.

export const usersApi = {
  list: (params?: { email?: string }) => api.get<User[]>('/users', params),
  get: (id: string) => api.get<User>(`/users/${id}`),
  update: (id: string, patch: Partial<User>) => api.put<User>(`/users/${id}`, patch),
};

export const habitsApi = {
  list: (userId: string) => api.get<Habit[]>('/habits', { userId }),
  get: (id: string) => api.get<Habit>(`/habits/${id}`),
  create: (data: Omit<Habit, 'id' | 'createdAt'>) => api.post<Habit>('/habits', data),
  update: (id: string, patch: Partial<Habit>) => api.put<Habit>(`/habits/${id}`, patch),
  remove: (id: string) => api.delete<Habit>(`/habits/${id}`),
};

export const checkinsApi = {
  listByHabit: (habitId: string) => api.get<Checkin[]>('/checkins', { habitId }),
  listAll: () => api.get<Checkin[]>('/checkins'),
  create: (data: Omit<Checkin, 'id' | 'createdAt'>) => api.post<Checkin>('/checkins', data),
  remove: (id: string) => api.delete<Checkin>(`/checkins/${id}`),
};

export const achievementsApi = {
  list: (userId: string) => api.get<Achievement[]>('/achievements', { userId }),
  create: (data: Omit<Achievement, 'id'>) => api.post<Achievement>('/achievements', data),
  markShared: (id: string) => api.put<Achievement>(`/achievements/${id}`, { shared: true }),
};
