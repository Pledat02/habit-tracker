package com.hehe.habit_tracker.entity;

import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.hehe.habit_tracker.common.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

// @NoArgsConstructor: Hibernate BẮT BUỘC có constructor rỗng để hydrate entity từ DB
// (thiếu sẽ crash lúc runtime, không phải lúc compile). @Data một mình chỉ sinh
// constructor theo các field @NonNull nên trước đây KHÔNG có constructor rỗng.
// Public để khớp các entity khác (Achivement, Checkin...) đang có constructor rỗng public mặc định.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull
    private String username;
    @NonNull
    @Email
    private String email;
    // Không @NonNull: tài khoản đăng nhập qua OAuth (Google...) không có mật khẩu tự tạo.
    private String password;
    // @Builder.Default: KHÔNG có nó, Users.builder().build() sẽ để role = null thay vì USER
    // (builder bỏ qua initializer field trừ khi được đánh dấu rõ).
    @Builder.Default
    private Role role = Role.USER;

    /** Email đã xác thực chưa (V8). User đăng ký mới = false tới khi bấm link xác thực;
     *  user cũ + đăng nhập Google = true. Không chặn đăng nhập, chỉ để hiển thị/nhắc. */
    @Builder.Default
    private boolean emailVerified = false;

    /** IANA timezone id (vd 'Asia/Ho_Chi_Minh'). Null -> code fallback default timezone.
     *  Dùng để tính "hôm nay" theo giờ user, không phải giờ server. Cột: zone_id (V2 migration). */
    private String zoneId;

    @OneToMany(mappedBy = "user")
    private List<UserAchivement> achivements;

    @OneToMany(mappedBy = "user")
    private List<Habit> habits;

    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}
