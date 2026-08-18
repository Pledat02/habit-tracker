import { createContext, useCallback, useContext, useRef, useState, type ReactNode } from 'react';
import { AnimatePresence, motion } from 'motion/react';
import { CheckCircle2, Info, X, AlertTriangle, XCircle } from 'lucide-react';
import { cn } from '@/lib/utils';

type ToastKind = 'success' | 'error' | 'info' | 'warning';
interface Toast {
  id: string;
  kind: ToastKind;
  message: string;
}

interface ToastCtx {
  success: (m: string) => void;
  error: (m: string) => void;
  info: (m: string) => void;
  warning: (m: string) => void;
}

const Ctx = createContext<ToastCtx | null>(null);

const ICONS = {
  success: CheckCircle2,
  error: XCircle,
  info: Info,
  warning: AlertTriangle,
} as const;

const ACCENT = {
  success: 'text-success',
  error: 'text-danger',
  info: 'text-secondary',
  warning: 'text-warning',
} as const;

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const timers = useRef<Record<string, number>>({});

  const dismiss = useCallback((id: string) => {
    setToasts((t) => t.filter((x) => x.id !== id));
    window.clearTimeout(timers.current[id]);
    delete timers.current[id];
  }, []);

  const push = useCallback(
    (kind: ToastKind, message: string) => {
      const id = Math.random().toString(36).slice(2);
      setToasts((t) => [...t, { id, kind, message }]);
      // Auto-dismiss after 4s; does not steal focus.
      timers.current[id] = window.setTimeout(() => dismiss(id), 4000);
    },
    [dismiss],
  );

  const value: ToastCtx = {
    success: (m) => push('success', m),
    error: (m) => push('error', m),
    info: (m) => push('info', m),
    warning: (m) => push('warning', m),
  };

  return (
    <Ctx.Provider value={value}>
      {children}
      <div
        className="pointer-events-none fixed inset-x-0 bottom-4 z-[100] flex flex-col items-center gap-2 px-4 sm:bottom-6"
        aria-live="polite"
        aria-atomic="true"
      >
        <AnimatePresence initial={false}>
          {toasts.map((t) => {
            const Icon = ICONS[t.kind];
            return (
              <motion.div
                key={t.id}
                layout
                role="status"
                className="pointer-events-auto flex w-full max-w-sm items-start gap-3 rounded-xl border border-border bg-surface p-3.5 shadow-soft-lg"
                initial={{ opacity: 0, y: 16, scale: 0.96 }}
                animate={{ opacity: 1, y: 0, scale: 1 }}
                exit={{ opacity: 0, scale: 0.96, transition: { duration: 0.15 } }}
                transition={{ type: 'spring', stiffness: 420, damping: 32 }}
              >
                <Icon className={cn('mt-0.5 h-5 w-5 shrink-0', ACCENT[t.kind])} aria-hidden />
                <p className="flex-1 text-sm text-foreground">{t.message}</p>
                <button
                  onClick={() => dismiss(t.id)}
                  className="focus-ring -m-1 rounded-md p-1 text-muted hover:text-foreground"
                  aria-label="Đóng thông báo"
                >
                  <X className="h-4 w-4" />
                </button>
              </motion.div>
            );
          })}
        </AnimatePresence>
      </div>
    </Ctx.Provider>
  );
}

export function useToast(): ToastCtx {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error('useToast must be used within ToastProvider');
  return ctx;
}
