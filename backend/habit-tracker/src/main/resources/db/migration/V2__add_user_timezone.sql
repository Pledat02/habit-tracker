-- Lưu timezone của user (IANA zone id, vd 'Asia/Ho_Chi_Minh').
-- Dùng để tính "hôm nay" theo giờ user thay vì giờ server (UTC) -> streak đúng.
-- Nullable: user cũ chưa có -> code fallback về default timezone.
alter table users add column zone_id varchar(64);
