import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { Loader2 } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { AppShell } from '@/components/layout/AppShell';
import { Auth } from '@/pages/Auth';
import { OAuth2Callback } from '@/pages/OAuth2Callback';
import { Onboarding, ONBOARDING_KEY } from '@/pages/Onboarding';
import { Dashboard } from '@/pages/Dashboard';
import { HabitsList } from '@/pages/HabitsList';
import { HabitDetail } from '@/pages/HabitDetail';
import { Insights } from '@/pages/Insights';
import { AchievementsPage } from '@/pages/AchievementsPage';
import { Profile } from '@/pages/Profile';

function FullScreenLoader() {
  return (
    <div className="flex min-h-[100dvh] items-center justify-center bg-background">
      <Loader2 className="h-8 w-8 animate-spin text-primary" />
    </div>
  );
}

/** Gate app routes behind auth + first-run onboarding. */
function RequireAuth({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();
  const location = useLocation();
  if (loading) return <FullScreenLoader />;
  if (!user) return <Navigate to="/auth" replace state={{ from: location }} />;
  if (!localStorage.getItem(ONBOARDING_KEY)) return <Navigate to="/onboarding" replace />;
  return <>{children}</>;
}

export default function App() {
  const { user, loading } = useAuth();

  return (
    <Routes>
      <Route path="/auth" element={user && !loading ? <Navigate to="/" replace /> : <Auth />} />
      <Route path="/oauth2/callback" element={<OAuth2Callback />} />
      <Route
        path="/onboarding"
        element={loading ? <FullScreenLoader /> : user ? <Onboarding /> : <Navigate to="/auth" replace />}
      />

      <Route
        path="/"
        element={
          <RequireAuth>
            <AppShell>
              <Dashboard />
            </AppShell>
          </RequireAuth>
        }
      />
      <Route
        path="/habits"
        element={
          <RequireAuth>
            <AppShell>
              <HabitsList />
            </AppShell>
          </RequireAuth>
        }
      />
      <Route
        path="/habits/:id"
        element={
          <RequireAuth>
            <AppShell>
              <HabitDetail />
            </AppShell>
          </RequireAuth>
        }
      />
      <Route
        path="/insights"
        element={
          <RequireAuth>
            <AppShell>
              <Insights />
            </AppShell>
          </RequireAuth>
        }
      />
      <Route
        path="/achievements"
        element={
          <RequireAuth>
            <AppShell>
              <AchievementsPage />
            </AppShell>
          </RequireAuth>
        }
      />
      <Route
        path="/profile"
        element={
          <RequireAuth>
            <AppShell>
              <Profile />
            </AppShell>
          </RequireAuth>
        }
      />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
