import { forwardRef } from 'react';
import { CheckCircle2 } from 'lucide-react';
import { Icon } from './ui/Icon';

export type ShareRatio = 'square' | 'story';

export const RATIO_DIM: Record<ShareRatio, { w: number; h: number }> = {
  square: { w: 1080, h: 1080 },
  story: { w: 1080, h: 1920 },
};

export interface ShareCardData {
  title: string; // habit name or recap title
  bigValue: string; // e.g. "30"
  bigLabel: string; // e.g. "ngày streak"
  caption: string; // tagline
  iconName: string; // milestone/decorative lucide icon
  habitIconName?: string; // optional small habit icon
  userName: string;
}

interface ShareCardProps {
  data: ShareCardData;
  ratio: ShareRatio;
}

/**
 * Rendered at full 1080px resolution and captured to PNG via html-to-image.
 * IMPORTANT: all colors are explicit hex/rgba — CSS theme tokens (rgb(var(--...)))
 * do not survive the canvas render, so we must not rely on Tailwind color tokens here.
 */
export const ShareCard = forwardRef<HTMLDivElement, ShareCardProps>(function ShareCard(
  { data, ratio },
  ref,
) {
  const { w, h } = RATIO_DIM[ratio];
  const story = ratio === 'story';

  const TEAL = '#0d9488';
  const TEAL_DARK = '#115e59';
  const MINT = '#ccfbf1';
  const MINT_SOFT = '#e6fffb';

  return (
    <div
      ref={ref}
      style={{
        width: w,
        height: h,
        position: 'relative',
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: story ? '160px 90px' : '90px',
        background: `linear-gradient(160deg, ${TEAL} 0%, #0f766e 55%, ${TEAL_DARK} 100%)`,
        fontFamily: 'Inter, system-ui, sans-serif',
        color: '#ffffff',
        boxSizing: 'border-box',
      }}
    >
      {/* Decorative circles */}
      <div style={{ position: 'absolute', top: -180, right: -160, width: 480, height: 480, borderRadius: '50%', background: 'rgba(255,255,255,0.08)' }} />
      <div style={{ position: 'absolute', bottom: -220, left: -180, width: 560, height: 560, borderRadius: '50%', background: 'rgba(255,255,255,0.06)' }} />

      {/* User */}
      <div style={{ fontSize: 34, fontWeight: 500, color: MINT, letterSpacing: 0.5, zIndex: 1 }}>
        {data.userName}
      </div>

      {/* Milestone icon */}
      <div
        style={{
          zIndex: 1,
          marginTop: story ? 80 : 44,
          width: story ? 300 : 240,
          height: story ? 300 : 240,
          borderRadius: '50%',
          background: 'rgba(255,255,255,0.16)',
          border: '6px solid rgba(255,255,255,0.28)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <Icon name={data.iconName} size={story ? 160 : 130} color="#ffffff" strokeWidth={1.75} />
      </div>

      {/* Big value */}
      <div style={{ zIndex: 1, marginTop: story ? 80 : 48, textAlign: 'center', lineHeight: 1 }}>
        <div style={{ fontFamily: 'Poppins, Inter, sans-serif', fontSize: story ? 300 : 240, fontWeight: 700, textShadow: '0 6px 30px rgba(0,0,0,0.18)' }}>
          {data.bigValue}
        </div>
        <div style={{ fontSize: story ? 56 : 48, fontWeight: 600, color: MINT, marginTop: 8 }}>
          {data.bigLabel}
        </div>
      </div>

      {/* Habit / caption */}
      <div style={{ zIndex: 1, marginTop: story ? 70 : 44, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 22 }}>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 18,
            background: 'rgba(255,255,255,0.14)',
            border: '2px solid rgba(255,255,255,0.22)',
            borderRadius: 9999,
            padding: story ? '20px 42px' : '16px 36px',
          }}
        >
          {data.habitIconName && <Icon name={data.habitIconName} size={story ? 46 : 40} color="#ffffff" />}
          <span style={{ fontSize: story ? 46 : 40, fontWeight: 600 }}>{data.title}</span>
        </div>
        <div style={{ fontSize: story ? 42 : 36, color: MINT_SOFT, fontWeight: 500, textAlign: 'center' }}>
          {data.caption}
        </div>
      </div>

      {/* App logo footer */}
      <div
        style={{
          position: 'absolute',
          bottom: story ? 90 : 56,
          display: 'flex',
          alignItems: 'center',
          gap: 14,
          zIndex: 1,
          opacity: 0.92,
        }}
      >
        <div
          style={{
            width: 48,
            height: 48,
            borderRadius: 12,
            background: '#ffffff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <CheckCircle2 size={30} color={TEAL} strokeWidth={2.5} />
        </div>
        <span style={{ fontFamily: 'Poppins, Inter, sans-serif', fontSize: 34, fontWeight: 700 }}>
          Habit Tracker
        </span>
      </div>
    </div>
  );
});
