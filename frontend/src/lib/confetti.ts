/** Tiny dependency-free confetti burst for streak milestones.
 *  Skipped entirely when the user prefers reduced motion. */
export function burstConfetti(origin?: { x: number; y: number }): void {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

  const colors = ['#0d9488', '#6366f1', '#16a34a', '#f59e0b', '#ef4444', '#2dd4bf'];
  const count = 28;
  const startX = origin?.x ?? window.innerWidth / 2;
  const startY = origin?.y ?? window.innerHeight / 3;

  const container = document.createElement('div');
  container.style.cssText =
    'position:fixed;inset:0;pointer-events:none;z-index:200;overflow:hidden';
  document.body.appendChild(container);

  for (let i = 0; i < count; i++) {
    const p = document.createElement('div');
    const size = 6 + Math.random() * 6;
    const angle = Math.random() * Math.PI * 2;
    const velocity = 60 + Math.random() * 120;
    const dx = Math.cos(angle) * velocity;
    const dy = Math.sin(angle) * velocity - 60;
    p.style.cssText = `position:absolute;left:${startX}px;top:${startY}px;width:${size}px;height:${size}px;background:${colors[i % colors.length]};border-radius:2px;opacity:1;will-change:transform,opacity;`;
    container.appendChild(p);

    const anim = p.animate(
      [
        { transform: 'translate(0,0) rotate(0deg)', opacity: 1 },
        {
          transform: `translate(${dx}px, ${dy + 260}px) rotate(${Math.random() * 720}deg)`,
          opacity: 0,
        },
      ],
      { duration: 900 + Math.random() * 500, easing: 'cubic-bezier(0.2,0.6,0.3,1)' },
    );
    anim.onfinish = () => p.remove();
  }

  setTimeout(() => container.remove(), 1800);
}
