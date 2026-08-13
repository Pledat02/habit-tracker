import { forwardRef, useId, type InputHTMLAttributes } from 'react';
import { cn } from '@/lib/utils';

interface FieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
  hint?: string;
  leftIcon?: React.ReactNode;
  rightSlot?: React.ReactNode;
}

/** Labeled input. Label is always visible (never placeholder-only);
 *  errors render directly below the field. */
export const Input = forwardRef<HTMLInputElement, FieldProps>(function Input(
  { label, error, hint, leftIcon, rightSlot, className, id, ...props },
  ref,
) {
  const autoId = useId();
  const inputId = id ?? autoId;
  const errId = `${inputId}-error`;
  const hintId = `${inputId}-hint`;

  return (
    <div className="w-full">
      <label htmlFor={inputId} className="mb-1.5 block text-sm font-medium text-foreground">
        {label}
      </label>
      <div className="relative">
        {leftIcon && (
          <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted">
            {leftIcon}
          </span>
        )}
        <input
          ref={ref}
          id={inputId}
          aria-invalid={!!error}
          aria-describedby={cn(error ? errId : undefined, hint ? hintId : undefined) || undefined}
          className={cn(
            'focus-ring h-11 w-full rounded-xl border bg-surface px-3.5 text-sm text-foreground placeholder:text-muted/70 transition-colors',
            !!leftIcon && 'pl-10',
            !!rightSlot && 'pr-11',
            error ? 'border-danger focus-visible:ring-danger/50' : 'border-border',
            className,
          )}
          {...props}
        />
        {rightSlot && <span className="absolute right-2 top-1/2 -translate-y-1/2">{rightSlot}</span>}
      </div>
      {error ? (
        <p id={errId} className="mt-1.5 text-xs font-medium text-danger">
          {error}
        </p>
      ) : hint ? (
        <p id={hintId} className="mt-1.5 text-xs text-muted">
          {hint}
        </p>
      ) : null}
    </div>
  );
});
