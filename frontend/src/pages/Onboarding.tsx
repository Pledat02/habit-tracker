import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRight, Check, Sparkles, Target, TrendingUp } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Icon } from '@/components/ui/Icon';
import { useCreateHabit } from '@/hooks/useHabits';
import { cn, encodeFrequency } from '@/lib/utils';
import { ONBOARDING_KEY } from '@/lib/onboarding';

export { ONBOARDING_KEY };

interface Slide {
  icon: React.ReactNode;
  title: string;
  desc: string;
}

const SLIDES: Slide[] = [
  {
    icon: <Target className="h-10 w-10" />,
    title: 'Xây thói quen, từng ngày một',
    desc: 'Chọn thói quen bạn muốn duy trì và check-in mỗi ngày. Đơn giản, nhẹ nhàng, không áp lực.',
  },
  {
    icon: <TrendingUp className="h-10 w-10" />,
    title: 'Giữ streak, thấy tiến bộ',
    desc: 'Chuỗi ngày liên tục và biểu đồ trực quan giúp bạn có động lực quay lại mỗi ngày.',
  },
  {
    icon: <Sparkles className="h-10 w-10" />,
    title: 'Thống kê rõ ràng',
    desc: 'Heatmap và insights cho bạn biết habit nào đang tốt, habit nào cần cố gắng thêm.',
  },
];

interface Goal {
  key: string;
  label: string;
  suggestions: { name: string; icon: string; color: string }[];
}

const GOALS: Goal[] = [
  {
    key: 'health',
    label: 'Sức khỏe',
    suggestions: [
      { name: 'Uống 2L nước', icon: 'GlassWater', color: 'primary' },
      { name: 'Tập thể dục', icon: 'Dumbbell', color: 'warning' },
      { name: 'Ngủ trước 23h', icon: 'Bed', color: 'secondary' },
    ],
  },
  {
    key: 'study',
    label: 'Học tập',
    suggestions: [
      { name: 'Đọc sách 20 phút', icon: 'BookOpen', color: 'secondary' },
      { name: 'Học từ vựng', icon: 'Languages', color: 'primary' },
      { name: 'Code 1 giờ', icon: 'Code2', color: 'success' },
    ],
  },
  {
    key: 'productivity',
    label: 'Năng suất',
    suggestions: [
      { name: 'Lập kế hoạch ngày', icon: 'PenLine', color: 'primary' },
      { name: 'Không dùng điện thoại buổi sáng', icon: 'Smartphone', color: 'danger' },
      { name: 'Dọn bàn làm việc', icon: 'Leaf', color: 'success' },
    ],
  },
  {
    key: 'mind',
    label: 'Khác',
    suggestions: [
      { name: 'Thiền 10 phút', icon: 'Brain', color: 'success' },
      { name: 'Viết nhật ký', icon: 'PenLine', color: 'secondary' },
      { name: 'Đi bộ', icon: 'Footprints', color: 'primary' },
    ],
  },
];

export function Onboarding() {
  const navigate = useNavigate();
  const create = useCreateHabit();
  const [step, setStep] = useState(0); // 0..2 slides, 3 = goal picker
  const [goal, setGoal] = useState<string | null>(null);
  const [picked, setPicked] = useState<Set<string>>(new Set());
  const [finishing, setFinishing] = useState(false);

  const activeGoal = GOALS.find((g) => g.key === goal);

  const finish = async (createHabits: boolean) => {
    setFinishing(true);
    try {
      if (createHabits && activeGoal) {
        const chosen = activeGoal.suggestions.filter((s) => picked.has(s.name));
        for (const s of chosen) {
          await create.mutateAsync({
            name: s.name,
            icon: s.icon,
            color: s.color,
            frequency: encodeFrequency('daily'),
            reminderTime: '',
            paused: false,
          });
        }
      }
      localStorage.setItem(ONBOARDING_KEY, '1');
      navigate('/');
    } finally {
      setFinishing(false);
    }
  };

  const isSlide = step < SLIDES.length;

  return (
    <div className="flex min-h-[100dvh] flex-col bg-background px-5 py-8">
      {/* Progress dots */}
      <div className="mb-8 flex items-center justify-center gap-2">
        {[0, 1, 2, 3].map((i) => (
          <span
            key={i}
            className={cn('h-1.5 rounded-full transition-all', i === step ? 'w-6 bg-primary' : 'w-1.5 bg-border')}
          />
        ))}
      </div>

      {isSlide ? (
        <div className="flex flex-1 flex-col items-center justify-center text-center animate-fade-in">
          <div className="mb-6 flex h-24 w-24 items-center justify-center rounded-3xl bg-primary/10 text-primary">
            {SLIDES[step].icon}
          </div>
          <h1 className="font-heading text-2xl font-bold text-foreground">{SLIDES[step].title}</h1>
          <p className="mt-3 max-w-sm text-muted">{SLIDES[step].desc}</p>
        </div>
      ) : (
        <div className="mx-auto flex w-full max-w-md flex-1 flex-col animate-fade-in">
          <h1 className="font-heading text-2xl font-bold text-foreground">Mục tiêu của bạn là gì?</h1>
          <p className="mt-1 text-sm text-muted">Chọn một lĩnh vực để chúng tôi gợi ý vài habit mẫu.</p>

          <div className="mt-5 grid grid-cols-2 gap-3">
            {GOALS.map((g) => (
              <button
                key={g.key}
                onClick={() => {
                  setGoal(g.key);
                  setPicked(new Set(g.suggestions.map((s) => s.name)));
                }}
                className={cn(
                  'focus-ring rounded-xl border p-4 text-left text-sm font-medium transition-colors',
                  goal === g.key ? 'border-primary bg-primary/10 text-primary' : 'border-border text-foreground hover:bg-surface-2',
                )}
              >
                {g.label}
              </button>
            ))}
          </div>

          {activeGoal && (
            <div className="mt-5 space-y-2 animate-fade-in">
              <p className="text-sm font-medium text-foreground">Gợi ý habit (chạm để chọn/bỏ):</p>
              {activeGoal.suggestions.map((s) => {
                const on = picked.has(s.name);
                return (
                  <button
                    key={s.name}
                    onClick={() =>
                      setPicked((cur) => {
                        const next = new Set(cur);
                        if (next.has(s.name)) next.delete(s.name);
                        else next.add(s.name);
                        return next;
                      })
                    }
                    className={cn(
                      'focus-ring flex w-full items-center gap-3 rounded-xl border p-3 text-left transition-colors',
                      on ? 'border-primary bg-primary/5' : 'border-border hover:bg-surface-2',
                    )}
                  >
                    <span className={cn('flex h-9 w-9 items-center justify-center rounded-lg', `bg-${s.color}/10`, `text-${s.color}`)}>
                      <Icon name={s.icon} className="h-5 w-5" />
                    </span>
                    <span className="flex-1 text-sm font-medium text-foreground">{s.name}</span>
                    <span className={cn('flex h-5 w-5 items-center justify-center rounded-full border', on ? 'border-primary bg-primary text-white' : 'border-border')}>
                      {on && <Check className="h-3.5 w-3.5" />}
                    </span>
                  </button>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* Actions */}
      <div className="mx-auto mt-8 w-full max-w-md space-y-3">
        {isSlide ? (
          <>
            <Button size="lg" className="w-full" onClick={() => setStep((s) => s + 1)}>
              Tiếp tục <ArrowRight className="h-5 w-5" />
            </Button>
            <button
              onClick={() => finish(false)}
              className="focus-ring w-full rounded-lg py-2 text-sm text-muted hover:text-foreground"
            >
              Bỏ qua
            </button>
          </>
        ) : (
          <>
            <Button
              size="lg"
              className="w-full"
              loading={finishing}
              disabled={!goal || picked.size === 0}
              onClick={() => finish(true)}
            >
              Tạo {picked.size} habit &amp; bắt đầu
            </Button>
            <button
              onClick={() => finish(false)}
              className="focus-ring w-full rounded-lg py-2 text-sm text-muted hover:text-foreground"
            >
              Để sau, vào thẳng app
            </button>
          </>
        )}
      </div>
    </div>
  );
}
