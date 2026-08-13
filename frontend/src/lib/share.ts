import { toBlob } from 'html-to-image';

/** App teal used as the card's flat background fallback during canvas render. */
export const SHARE_BG = '#0d9488';

/** Render a DOM node to a PNG blob at an exact pixel size.
 *  Throws on failure so callers can surface a toast without crashing check-in. */
export async function nodeToPngBlob(node: HTMLElement, width: number, height: number): Promise<Blob> {
  const blob = await toBlob(node, {
    width,
    height,
    pixelRatio: 1, // node is already authored at full 1080px resolution
    cacheBust: true,
    backgroundColor: SHARE_BG,
    // Neutralize any preview scaling on the captured node.
    style: { transform: 'none', transformOrigin: 'top left', margin: '0' },
  });
  if (!blob) throw new Error('Không tạo được ảnh (canvas render lỗi)');
  return blob;
}

/** Can the browser share files via the native share sheet? */
export function canShareFiles(blob?: Blob): boolean {
  if (typeof navigator === 'undefined' || !navigator.share || !navigator.canShare) return false;
  if (!blob) return true;
  try {
    const file = new File([blob], 'achievement.png', { type: 'image/png' });
    return navigator.canShare({ files: [file] });
  } catch {
    return false;
  }
}

export async function webShareImage(blob: Blob, title: string, text: string): Promise<boolean> {
  const file = new File([blob], 'habit-achievement.png', { type: 'image/png' });
  if (!canShareFiles(blob)) return false;
  try {
    await navigator.share({ files: [file], title, text });
    return true;
  } catch (err) {
    // AbortError = user cancelled the sheet — not a real failure.
    if (err instanceof DOMException && err.name === 'AbortError') return false;
    throw err;
  }
}

export function downloadBlob(blob: Blob, filename = 'habit-achievement.png'): void {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

export function canCopyImage(): boolean {
  return (
    typeof navigator !== 'undefined' &&
    !!navigator.clipboard &&
    typeof window.ClipboardItem !== 'undefined' &&
    typeof navigator.clipboard.write === 'function'
  );
}

export async function copyBlobToClipboard(blob: Blob): Promise<void> {
  const item = new ClipboardItem({ 'image/png': blob });
  await navigator.clipboard.write([item]);
}
