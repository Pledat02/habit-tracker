import type { ReactNode } from 'react';

interface EmptyStateProps {
  icon: ReactNode;
  title: string;
  description?: string;
  action?: ReactNode;
}

export function EmptyState({ icon, title, description, action }: EmptyStateProps) {
  return (
    <div className="relative flex flex-col items-center justify-center overflow-hidden rounded-2xl border border-dashed border-border bg-surface/50 px-6 py-14 text-center animate-fade-in">
      {/* Background blur blob for depth */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 h-32 w-32 rounded-full bg-primary/10 blur-[40px]" />
      
      <div className="relative mb-5 flex h-16 w-16 items-center justify-center rounded-2xl bg-primary/10 text-primary shadow-soft">
        {icon}
      </div>
      <h3 className="relative text-lg font-bold text-foreground">{title}</h3>
      {description && <p className="relative mt-2 max-w-sm text-sm text-muted">{description}</p>}
      {action && <div className="relative mt-8">{action}</div>}
    </div>
  );
}
