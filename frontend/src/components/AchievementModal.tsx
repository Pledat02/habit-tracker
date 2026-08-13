import { useEffect, useRef, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { Copy, Download, PartyPopper, Share2 } from 'lucide-react';
import { Modal } from './ui/Modal';
import { Button } from './ui/Button';
import { ShareCard, RATIO_DIM, type ShareCardData, type ShareRatio } from './ShareCard';
import { useToast } from '@/context/ToastContext';
import { achievementsApi } from '@/lib/resources';
import {
  canCopyImage,
  canShareFiles,
  copyBlobToClipboard,
  downloadBlob,
  nodeToPngBlob,
  webShareImage,
} from '@/lib/share';
import { burstConfetti } from '@/lib/confetti';
import { cn } from '@/lib/utils';

export interface SharePayload {
  data: ShareCardData;
  modalTitle: string;
  celebrate: boolean; // show confetti + congrats copy (milestone unlock)
  shareTitle: string;
  shareText: string;
  achievementId?: string; // when present, mark shared=true after a successful share
}

interface AchievementModalProps {
  open: boolean;
  payload: SharePayload | null;
  onClose: () => void;
}

const PREVIEW_W = 260;

export function AchievementModal({ open, payload, onClose }: AchievementModalProps) {
  const [ratio, setRatio] = useState<ShareRatio>('square');
  const [busy, setBusy] = useState<null | 'share' | 'download' | 'copy'>(null);
  const captureRef = useRef<HTMLDivElement>(null);
  const toast = useToast();
  const qc = useQueryClient();

  // Confetti on unlock (respects reduced motion inside burstConfetti).
  useEffect(() => {
    if (open && payload?.celebrate) {
      const t = setTimeout(() => burstConfetti(), 120);
      return () => clearTimeout(t);
    }
  }, [open, payload?.celebrate]);

  useEffect(() => {
    if (open) setRatio('square');
  }, [open]);

  if (!open || !payload) return null;

  const { w, h } = RATIO_DIM[ratio];
  const scale = PREVIEW_W / w;

  const markSharedIfNeeded = async () => {
    if (!payload.achievementId) return;
    try {
      await achievementsApi.markShared(payload.achievementId);
      qc.invalidateQueries({ queryKey: ['achievements'] });
    } catch {
      /* stats-only, ignore failures */
    }
  };

  const render = async (): Promise<Blob> => {
    if (!captureRef.current) throw new Error('Chưa sẵn sàng để tạo ảnh');
    return nodeToPngBlob(captureRef.current, w, h);
  };

  const onNativeShare = async () => {
    setBusy('share');
    try {
      const blob = await render();
      const ok = await webShareImage(blob, payload.shareTitle, payload.shareText);
      if (ok) {
        await markSharedIfNeeded();
        toast.success('Đã chia sẻ thành tựu!');
        onClose();
      }
    } catch {
      toast.error('Không tạo/chia sẻ được ảnh, thử lại nhé');
    } finally {
      setBusy(null);
    }
  };

  const onDownload = async () => {
    setBusy('download');
    try {
      const blob = await render();
      downloadBlob(blob, `streak-${payload.data.bigValue}.png`);
      await markSharedIfNeeded();
      toast.success('Đã tải ảnh xuống');
    } catch {
      toast.error('Không tạo được ảnh, thử lại nhé');
    } finally {
      setBusy(null);
    }
  };

  const onCopy = async () => {
    setBusy('copy');
    try {
      const blob = await render();
      await copyBlobToClipboard(blob);
      await markSharedIfNeeded();
      toast.success('Đã copy ảnh vào clipboard');
    } catch {
      toast.error('Trình duyệt không cho phép copy ảnh, hãy tải xuống thay thế');
    } finally {
      setBusy(null);
    }
  };

  const supportsShare = canShareFiles();
  const supportsCopy = canCopyImage();

  return (
    <Modal open={open} onClose={onClose} title={payload.modalTitle} size="lg">
      {payload.celebrate && (
        <div className="mb-4 flex items-center gap-3 rounded-xl bg-primary/10 p-3.5 text-primary animate-pop-in">
          <PartyPopper className="h-6 w-6 shrink-0" />
          <p className="text-sm font-medium">
            Bạn vừa mở khóa một cột mốc mới. Chia sẻ để giữ lửa và rủ bạn bè cùng cố gắng!
          </p>
        </div>
      )}

      {/* Ratio toggle */}
      <div className="mb-4 flex justify-center gap-2">
        {(['square', 'story'] as ShareRatio[]).map((r) => (
          <button
            key={r}
            onClick={() => setRatio(r)}
            aria-pressed={ratio === r}
            className={cn(
              'focus-ring rounded-full px-4 py-2 text-sm font-medium transition-colors',
              ratio === r ? 'bg-primary text-primary-foreground' : 'bg-surface-2 text-muted hover:text-foreground',
            )}
          >
            {r === 'square' ? 'Vuông 1080×1080' : 'Story 1080×1920'}
          </button>
        ))}
      </div>

      {/* Preview (scaled). */}
      <div className="flex justify-center">
        <div
          className="overflow-hidden rounded-2xl shadow-soft-lg"
          style={{ width: PREVIEW_W, height: h * scale }}
        >
          <div style={{ width: w, height: h, transform: `scale(${scale})`, transformOrigin: 'top left' }}>
            <ShareCard data={payload.data} ratio={ratio} />
          </div>
        </div>
      </div>

      {/* Hidden full-size node used for capture. Kept in DOM (not display:none) so
          html-to-image can measure & paint it. */}
      <div style={{ position: 'fixed', left: -99999, top: 0, pointerEvents: 'none', opacity: 0 }} aria-hidden>
        <ShareCard ref={captureRef} data={payload.data} ratio={ratio} />
      </div>

      {/* Actions */}
      <div className="mt-6 space-y-2.5">
        {supportsShare && (
          <Button size="lg" className="w-full" onClick={onNativeShare} loading={busy === 'share'} disabled={!!busy}>
            <Share2 className="h-5 w-5" /> Chia sẻ ngay
          </Button>
        )}
        <div className="flex gap-2.5">
          <Button
            variant={supportsShare ? 'outline' : 'primary'}
            size="lg"
            className="flex-1"
            onClick={onDownload}
            loading={busy === 'download'}
            disabled={!!busy}
          >
            <Download className="h-5 w-5" /> Tải ảnh
          </Button>
          {supportsCopy && (
            <Button variant="outline" size="lg" className="flex-1" onClick={onCopy} loading={busy === 'copy'} disabled={!!busy}>
              <Copy className="h-5 w-5" /> Copy ảnh
            </Button>
          )}
        </div>
        <button
          onClick={onClose}
          className="focus-ring w-full rounded-lg py-2.5 text-sm font-medium text-muted hover:text-foreground"
        >
          {payload.celebrate ? 'Để sau' : 'Đóng'}
        </button>
      </div>
    </Modal>
  );
}
