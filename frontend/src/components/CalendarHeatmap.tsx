import { useMemo } from 'react';
import { addDays, toDateKey } from '@/lib/utils';

interface CalendarHeatmapProps {
  /** Set of 'YYYY-MM-DD' keys that are completed. */
  done: Set<string>;
  weeks?: number;
  color?: string; // token key
}

const DOW = ['', 'T2', '', 'T4', '', 'T6', ''];
const MONTHS = ['Th1', 'Th2', 'Th3', 'Th4', 'Th5', 'Th6', 'Th7', 'Th8', 'Th9', 'Th10', 'Th11', 'Th12'];

/** GitHub-contribution-style heatmap. Columns = weeks, rows = days (Sun..Sat). */
export function CalendarHeatmap({ done, weeks = 17, color = 'primary' }: CalendarHeatmapProps) {
  const { columns, monthLabels } = useMemo(() => {
    const today = new Date();
    // Start from the Sunday `weeks` weeks ago.
    const start = addDays(today, -(weeks * 7 - 1));
    start.setDate(start.getDate() - start.getDay()); // back to Sunday

    const cols: { key: string; date: Date; filled: boolean; future: boolean }[][] = [];
    const labels: { col: number; text: string }[] = [];
    let lastMonth = -1;

    const cursor = new Date(start);
    for (let w = 0; w < weeks + 1; w++) {
      const col: { key: string; date: Date; filled: boolean; future: boolean }[] = [];
      for (let d = 0; d < 7; d++) {
        const key = toDateKey(cursor);
        const future = cursor.getTime() > today.getTime();
        col.push({ key, date: new Date(cursor), filled: done.has(key), future });
        if (d === 0 && cursor.getMonth() !== lastMonth) {
          lastMonth = cursor.getMonth();
          labels.push({ col: w, text: MONTHS[cursor.getMonth()] });
        }
        cursor.setDate(cursor.getDate() + 1);
      }
      cols.push(col);
    }
    return { columns: cols, monthLabels: labels };
  }, [done, weeks]);

  return (
    <div className="overflow-x-auto">
      <div className="inline-flex flex-col gap-1">
        {/* Month labels */}
        <div className="ml-8 flex gap-1 text-[10px] text-muted">
          {columns.map((_, i) => {
            const lbl = monthLabels.find((m) => m.col === i);
            return (
              <div key={i} className="w-3.5 shrink-0">
                {lbl?.text ?? ''}
              </div>
            );
          })}
        </div>
        <div className="flex gap-1">
          {/* Day-of-week labels */}
          <div className="mr-1 flex w-7 flex-col gap-1 text-[10px] text-muted">
            {DOW.map((d, i) => (
              <div key={i} className="flex h-3.5 items-center justify-end">
                {d}
              </div>
            ))}
          </div>
          {/* Columns */}
          {columns.map((col, ci) => (
            <div key={ci} className="flex flex-col gap-1">
              {col.map((cell) => (
                <div
                  key={cell.key}
                  title={cell.future ? '' : `${cell.key}${cell.filled ? ' · hoàn thành' : ''}`}
                  className={[
                    'h-3.5 w-3.5 rounded-[3px] transition-colors',
                    cell.future
                      ? 'bg-transparent'
                      : cell.filled
                        ? `bg-${color}`
                        : 'bg-surface-2',
                  ].join(' ')}
                />
              ))}
            </div>
          ))}
        </div>
        {/* Legend */}
        <div className="ml-8 mt-1 flex items-center gap-1.5 text-[10px] text-muted">
          <span>Ít</span>
          <span className="h-3 w-3 rounded-[3px] bg-surface-2" />
          <span className={`h-3 w-3 rounded-[3px] bg-${color}/40`} />
          <span className={`h-3 w-3 rounded-[3px] bg-${color}`} />
          <span>Nhiều</span>
        </div>
      </div>
    </div>
  );
}
