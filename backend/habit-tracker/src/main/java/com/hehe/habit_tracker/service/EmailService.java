package com.hehe.habit_tracker.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Gửi email. Thiết kế LINH HOẠT để dev test được ngay mà không cần SMTP:
 *  - Có cấu hình SMTP (spring.mail.username khác rỗng) -> gửi thật qua JavaMailSender.
 *  - Chưa cấu hình -> LOG nội dung (link reset) ra console, không ném lỗi.
 * Nhờ vậy luồng chạy end-to-end cả khi chưa có Gmail App Password; cắm SMTP vào là tự gửi thật.
 */
@Service
@Slf4j
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.from:${spring.mail.username:no-reply@habit-tracker.local}}")
    private String from;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    private boolean smtpConfigured() {
        return mailUsername != null && !mailUsername.isBlank();
    }

    public void send(String to, String subject, String body) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (!smtpConfigured() || sender == null) {
            // Fallback dev: không có SMTP -> in ra log để test được luồng.
            log.warn("SMTP chưa cấu hình — không gửi email thật. Nội dung cho [{}]:\n--- {} ---\n{}",
                    to, subject, body);
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        sender.send(msg);
        log.info("Đã gửi email '{}' tới {}", subject, to);
    }
}
