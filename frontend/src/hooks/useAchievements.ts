import { useQuery } from '@tanstack/react-query';
import { achievementsApi } from '@/lib/resources';
import { useAuth } from '@/context/AuthContext';

/** Fetch all achievements for the current user. */
export function useAchievements() {
  const { user } = useAuth();
  return useQuery({
    queryKey: ['achievements', user?.id],
    queryFn: () => achievementsApi.list(user!.id),
    enabled: !!user,
  });
}
