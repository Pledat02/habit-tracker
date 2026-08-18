-- Index cho habits.user_id: mọi truy vấn "của user này" (findByUserId, và /checkins/me
-- lọc theo khoảng ngày phải join habits theo user_id trước) đều lọc theo cột này.
-- Bảng checkins đã có unique index (habit_id, checkin_date) từ V1 nên range-scan theo ngày
-- đã nhanh; chỉ còn thiếu index bên habits.
create index if not exists idx_habits_user on habits (user_id);
