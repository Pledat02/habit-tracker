/**
 * Web Push phía client: đăng ký service worker, xin quyền, subscribe qua PushManager
 * với VAPID public key lấy từ backend, rồi gửi subscription lên server.
 */
import { pushApi } from './resources';

export function isPushSupported(): boolean {
  return (
    typeof navigator !== 'undefined' &&
    'serviceWorker' in navigator &&
    typeof window !== 'undefined' &&
    'PushManager' in window &&
    'Notification' in window
  );
}

/** VAPID key base64url -> Uint8Array (định dạng applicationServerKey yêu cầu).
 *  Cấp phát ArrayBuffer tường minh để kiểu là Uint8Array<ArrayBuffer> (hợp BufferSource). */
function urlBase64ToUint8Array(base64String: string): Uint8Array<ArrayBuffer> {
  const padding = '='.repeat((4 - (base64String.length % 4)) % 4);
  const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
  const raw = atob(base64);
  const output = new Uint8Array(new ArrayBuffer(raw.length));
  for (let i = 0; i < raw.length; i++) output[i] = raw.charCodeAt(i);
  return output;
}

async function getRegistration(): Promise<ServiceWorkerRegistration> {
  return navigator.serviceWorker.register('/sw.js');
}

/** true nếu đang có subscription hoạt động (để hiển thị trạng thái toggle). */
export async function isPushEnabled(): Promise<boolean> {
  if (!isPushSupported()) return false;
  const reg = await navigator.serviceWorker.getRegistration();
  const sub = await reg?.pushManager.getSubscription();
  return !!sub;
}

/** Bật push: xin quyền -> subscribe -> gửi lên backend. Trả false nếu không hỗ trợ / bị từ chối. */
export async function enablePush(): Promise<boolean> {
  if (!isPushSupported()) return false;

  const permission = await Notification.requestPermission();
  if (permission !== 'granted') return false;

  const reg = await getRegistration();
  const publicKey = await pushApi.getPublicKey();
  if (!publicKey) return false;

  const sub = await reg.pushManager.subscribe({
    userVisibleOnly: true,
    applicationServerKey: urlBase64ToUint8Array(publicKey),
  });

  const json = sub.toJSON();
  await pushApi.subscribe({
    endpoint: sub.endpoint,
    p256dh: json.keys?.p256dh ?? '',
    auth: json.keys?.auth ?? '',
  });
  return true;
}

/** Tắt push: huỷ subscription ở trình duyệt + báo backend xoá. */
export async function disablePush(): Promise<void> {
  if (!isPushSupported()) return;
  const reg = await navigator.serviceWorker.getRegistration();
  const sub = await reg?.pushManager.getSubscription();
  if (sub) {
    await pushApi.unsubscribe(sub.endpoint).catch(() => {});
    await sub.unsubscribe().catch(() => {});
  }
}
