package com.hehe.habit_tracker.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Rate limiter in-memory kiểu token bucket, tự code (không thêm dependency như bucket4j).
 * Mỗi key (thường là IP) có 1 bucket: đầy {@code capacity} token, hồi {@code refillPerMinute}
 * token mỗi phút. Mỗi request tiêu 1 token; hết token -> bị chặn (429).
 *
 * Phù hợp deploy 1 instance. Nếu scale nhiều instance thì mỗi instance đếm riêng — khi đó
 * nên chuyển sang store tập trung (Redis). Với quy mô hiện tại, in-memory là đủ và rẻ nhất.
 */
@Component
public class RateLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.auth.capacity:5}")
    private int capacity;

    @Value("${app.rate-limit.auth.refill-per-minute:5}")
    private double refillPerMinute;

    /** Tiêu 1 token cho {@code key}; trả false nếu đã cạn (nên chặn request). */
    public boolean tryAcquire(String key) {
        Bucket b = buckets.computeIfAbsent(key, k -> new Bucket(capacity));
        return b.tryConsume(capacity, refillPerMinute);
    }

    /**
     * Dọn định kỳ những bucket đã hồi đầy (chủ của nó đã ngưng gọi đủ lâu) -> quên đi an toàn,
     * tránh map phình theo số IP từng ghé. Bucket đầy nghĩa là "như chưa từng bị giới hạn".
     */
    @Scheduled(fixedDelayString = "${app.rate-limit.cleanup-ms:600000}")
    public void evictFullBuckets() {
        long now = System.nanoTime();
        buckets.forEach((key, b) -> {
            if (b.isFull(capacity, refillPerMinute, now)) {
                buckets.remove(key, b);
            }
        });
    }

    /** Token bucket cho 1 key. Đồng bộ ở mức bucket để nhiều request cùng key không đua nhau. */
    private static final class Bucket {
        private double tokens;
        private long lastRefillNanos;

        Bucket(int capacity) {
            this.tokens = capacity;
            this.lastRefillNanos = System.nanoTime();
        }

        synchronized boolean tryConsume(int capacity, double refillPerMinute) {
            refill(capacity, refillPerMinute, System.nanoTime());
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        synchronized boolean isFull(int capacity, double refillPerMinute, long now) {
            refill(capacity, refillPerMinute, now);
            return tokens >= capacity;
        }

        private void refill(int capacity, double refillPerMinute, long now) {
            double elapsedMinutes = (now - lastRefillNanos) / 60_000_000_000.0;
            if (elapsedMinutes > 0) {
                tokens = Math.min(capacity, tokens + elapsedMinutes * refillPerMinute);
                lastRefillNanos = now;
            }
        }
    }
}
