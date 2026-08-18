package com.hehe.habit_tracker.service;

import java.security.Security;

import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hehe.habit_tracker.entity.PushSubscription;
import com.hehe.habit_tracker.repository.PushSubscriptionRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;

/**
 * Gửi Web Push notification qua VAPID. Nếu chưa cấu hình khoá (private key rỗng) thì
 * TẮT hẳn (isEnabled=false, send() no-op) — app vẫn chạy bình thường không có push.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WebPushService {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    @Value("${app.push.vapid-public-key:}")
    private String publicKey;
    @Value("${app.push.vapid-private-key:}")
    private String privateKey;
    @Value("${app.push.vapid-subject:mailto:admin@habit-tracker.local}")
    private String subject;

    private PushService pushService;

    @PostConstruct
    void init() {
        if (privateKey == null || privateKey.isBlank()) {
            log.info("Web Push: chưa cấu hình VAPID key -> tính năng push TẮT");
            return;
        }
        try {
            Security.addProvider(new BouncyCastleProvider());
            pushService = new PushService(publicKey, privateKey, subject);
            log.info("Web Push: đã bật");
        } catch (Exception e) {
            log.warn("Web Push: khởi tạo PushService lỗi -> tắt push", e);
        }
    }

    public boolean isEnabled() {
        return pushService != null;
    }

    /**
     * Gửi 1 notification tới 1 subscription. Nếu push service trả 404/410 (subscription
     * hết hạn/không còn) thì XOÁ nó khỏi DB để lần sau khỏi gửi nữa.
     */
    public void send(PushSubscription sub, String title, String body, String url) {
        if (pushService == null) {
            return;
        }
        String payload = "{\"title\":\"" + esc(title) + "\",\"body\":\"" + esc(body)
                + "\",\"url\":\"" + esc(url) + "\"}";
        try {
            Subscription s = new Subscription(sub.getEndpoint(),
                    new Subscription.Keys(sub.getP256dh(), sub.getAuth()));
            HttpResponse resp = pushService.send(new Notification(s, payload));
            int code = resp.getStatusLine().getStatusCode();
            if (code == 404 || code == 410) {
                pushSubscriptionRepository.delete(sub); // subscription chết -> dọn
            }
        } catch (Exception e) {
            log.warn("Web Push: gửi tới endpoint {} lỗi", sub.getEndpoint(), e);
        }
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
