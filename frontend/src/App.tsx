import { lazy, Suspense } from 'react';
import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { Loader2 } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { AppShell } from '@/components/layout/AppShell';

// Mỗi page tách thành chunk riêng (React.lazy) -> tải theo route thay vì gộp hết vào 1 bundle.
// ONBOARDING_KEY ở module nhẹ riêng (lib/onboarding) nên import tĩnh không kéo page Onboarding vào bundle đầu.
import { ONBOARDING_KEY } from '@/lib/onboarding';
const Auth = lazy(() => import('@/pages/Auth').then((m) => ({ default: m.Auth })));
const ForgotPassword = lazy(() => import('@/pages/ForgotPassword').then((m) => ({ default: m.ForgotPassword })));
const ResetPassword = lazy(() => import('@/pages/ResetPassword').then((m) => ({ default: m.ResetPassword })));
const OAuth2Callback = lazy(() => import('@/pages/OAuth2Callback').then((m) => ({ default: m.OAuth2Callback })));
const Onboarding = lazy(() => import('@/pages/Onboarding').then((m) => ({ default: m.Onboarding })));
const Dashboard = lazy(() => import('@/pages/Dashboard').then((m) => ({ default: m.Dashboard })));
const HabitsList = lazy(() => import('@/pages/HabitsList').then((m) => ({ default: m.HabitsList })));
const HabitDetail = lazy(() => import('@/pages/HabitDetail').then((m) => ({ default: m.HabitDetail })));
const Insights = lazy(() => import('@/pages/Insights').then((m) => ({ default: m.Insights })));
const AchievementsPage = lazy(() => import('@/pages/AchievementsPage').then((m) => ({ default: m.AchievementsPage })));
const Profile = lazy(() => import('@/pages/Profile').then((m) => ({ default: m.Profile })));

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
    <Suspense fallback={<FullScreenLoader />}>
    <Routes>
      <Route path="/auth" element={user && !loading ? <Navigate to="/" replace /> : <Auth />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/reset-password" element={<ResetPassword />} />
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
    </Suspense>
  );
}
