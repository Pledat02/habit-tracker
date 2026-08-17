import { useState, type ReactNode } from 'react';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import { AnimatePresence, motion, useReducedMotion } from 'motion/react';
import { CheckCircle2, Plus, Trophy } from 'lucide-react';
import { NAV_ITEMS } from './nav';
import { cn } from '@/lib/utils';
import { HabitFormModal } from '@/components/HabitFormModal';

/** Responsive shell: fixed left sidebar on desktop, bottom nav on mobile,
 *  plus a floating create button. */
export function AppShell({ children }: { children: ReactNode }) {
  const [createOpen, setCreateOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const reduceMotion = useReducedMotion();

  return (
    <div className="min-h-[100dvh] bg-background">
      {/* Desktop sidebar */}
      <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 flex-col border-r border-border bg-surface px-4 py-6 lg:flex">
        <div className="mb-8 flex items-center gap-2.5 px-2">
          <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-primary text-primary-foreground">
            <CheckCircle2 className="h-5 w-5" />
          </div>
          <span className="font-heading text-lg font-bold text-foreground">Habit Tracker</span>
        </div>

        <nav className="flex flex-1 flex-col gap-1">
          {NAV_ITEMS.filter((i) => !i.action).map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) =>
                cn(
                  'focus-ring flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors',
                  isActive
                    ? 'bg-primary/10 text-primary'
                    : 'text-muted hover:bg-surface-2 hover:text-foreground',
                )
              }
            >
              <item.icon className="h-5 w-5" />
              {item.label}
            </NavLink>
          ))}
          <NavLink
            to="/achievements"
            className={({ isActive }) =>
              cn(
                'focus-ring flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors',
                isActive ? 'bg-primary/10 text-primary' : 'text-muted hover:bg-surface-2 hover:text-foreground',
              )
            }
          >
            <Trophy className="h-5 w-5" />
            Thành tựu
          </NavLink>
        </nav>

        <button
          onClick={() => setCreateOpen(true)}
          className="focus-ring mt-2 flex items-center justify-center gap-2 rounded-xl bg-primary px-4 py-3 text-sm font-semibold text-primary-foreground shadow-soft transition-opacity hover:opacity-90"
        >
          <Plus className="h-5 w-5" />
          Tạo habit
        </button>
      </aside>

      {/* Main column */}
      <div className="lg:pl-64">
        <main className="mx-auto w-full max-w-6xl px-4 pb-28 pt-5 sm:px-6 lg:pb-10 lg:pt-8">
          <AnimatePresence mode="wait">
            <motion.div
              key={location.pathname}
              initial={reduceMotion ? { opacity: 0 } : { opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={reduceMotion ? { opacity: 0 } : { opacity: 0, y: -8 }}
              transition={{ duration: 0.18, ease: 'easeOut' }}
            >
              {children}
            </motion.div>
          </AnimatePresence>
        </main>
      </div>

      {/* Mobile bottom nav */}
      <nav className="fixed inset-x-0 bottom-0 z-30 border-t border-border bg-surface/95 backdrop-blur pb-[env(safe-area-inset-bottom)] lg:hidden">
        <div className="mx-auto flex max-w-md items-stretch justify-around">
          {NAV_ITEMS.map((item) =>
            item.action === 'create' ? (
              <button
                key={item.to}
                onClick={() => setCreateOpen(true)}
                aria-label="Tạo habit mới"
                className="focus-ring flex flex-1 flex-col items-center justify-center py-1.5"
              >
                <span className="flex h-11 w-11 items-center justify-center rounded-full bg-primary text-primary-foreground shadow-soft">
                  <Plus className="h-6 w-6" />
                </span>
              </button>
            ) : (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/'}
                className={({ isActive }) =>
                  cn(
                    'focus-ring relative flex flex-1 flex-col items-center gap-1 py-2 text-[10px] font-medium transition-all active:scale-95',
                    isActive ? 'text-primary' : 'text-muted hover:text-foreground',
                  )
                }
              >
                {({ isActive }) => (
                  <>
                    <item.icon className={cn('h-5 w-5 transition-transform', isActive && 'scale-110')} />
                    <span>{item.label}</span>
                    {isActive && (
                      <span className="absolute bottom-0.5 h-1 w-1 rounded-full bg-primary animate-fade-in" />
                    )}
                  </>
                )}
              </NavLink>
            ),
          )}
        </div>
      </nav>

      {/* Floating action button (desktop, since sidebar has one too it's mobile-hidden via bottom nav) */}
      <button
        onClick={() => setCreateOpen(true)}
        aria-label="Tạo habit mới"
        className="focus-ring fixed bottom-24 right-5 z-20 hidden h-14 w-14 items-center justify-center rounded-full bg-primary text-primary-foreground shadow-soft-lg transition-transform hover:scale-105 active:scale-95 sm:flex lg:hidden"
      >
        <Plus className="h-6 w-6" />
      </button>

      <HabitFormModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreated={(id) => {
          setCreateOpen(false);
          navigate(`/habits/${id}`);
        }}
      />
    </div>
  );
}
