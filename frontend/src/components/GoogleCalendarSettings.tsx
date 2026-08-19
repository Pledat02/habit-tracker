import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Calendar, RefreshCw } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { useToast } from '@/context/ToastContext';
import { calendarApi } from '@/lib/resources';

/**
 * Kết nối Google Calendar (đồng bộ 1 chiều habit -> Calendar). Ẩn hẳn nếu tính năng chưa
 * bật ở backend (flag). Kết nối = chuyển hướng sang Google; Google gọi callback backend rồi
 * quay lại /profile?calendar=connected.
 */
export function GoogleCalendarSettings() {
  const toast = useToast();
  const [params, setParams] = useSearchParams();
  const [state, setState] = useState<{ enabled: boolean; connected: boolean } | null>(null);
  const [busy, setBusy] = useState(false);

  const refresh = () => calendarApi.status().then(setState).catch(() => setState({ enabled: false, connected: false }));

  useEffect(() => {
    refresh();
  }, []);

  // Quay lại từ Google với ?calendar=connected|error -> báo + làm mới trạng thái.
  useEffect(() => {
    const result = params.get('calendar');
    if (result === 'connected' || result === 'error') {
      if (result === 'connected') {
        toast.success('Đã kết nối Google Calendar.');
        refresh();
      } else {
        toast.error('Kết nối Google Calendar không thành công.');
      }
      params.delete('calendar');
      setParams(params, { replace: true });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (!state || !state.enabled) return null; // flag tắt -> không hiện gì

  const connect = async () => {
    setBusy(true);
    try {
      window.location.href = await calendarApi.authorizeUrl();
    } catch {
      toast.error('Không mở được trang cấp quyền Google.');
      setBusy(false);
    }
  };

  const disconnect = async () => {
    setBusy(true);
    try {
      await calendarApi.disconnect();
      toast.success('Đã ngắt kết nối Google Calendar.');
      await refresh();
    } finally {
      setBusy(false);
    }
  };

  const syncNow = async () => {
    setBusy(true);
    try {
      await calendarApi.sync();
      toast.success('Đã đẩy các thói quen lên Google Calendar.');
    } catch {
      toast.error('Đồng bộ thất bại, thử lại sau.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <section className="card p-5">
      <div className="mb-3 flex items-center gap-2">
        <Calendar className="h-5 w-5 text-primary" />
        <h2 className="font-heading text-base font-semibold text-foreground">Google Calendar</h2>
      </div>
      <p className="mb-4 text-sm text-muted">
        Đồng bộ 1 chiều: mỗi thói quen có giờ nhắc sẽ tạo sự kiện lặp lại trong Google Calendar của bạn.
      </p>

      {state.connected ? (
        <div className="flex flex-wrap gap-2">
          <Button variant="outline" onClick={syncNow} disabled={busy}>
            <RefreshCw className="h-4 w-4" /> Đồng bộ ngay
          </Button>
          <Button variant="ghost" onClick={disconnect} disabled={busy}>
            Ngắt kết nối
          </Button>
        </div>
      ) : (
        <Button onClick={connect} disabled={busy}>
          <Calendar className="h-4 w-4" /> Kết nối Google Calendar
        </Button>
      )}
    </section>
  );
}
