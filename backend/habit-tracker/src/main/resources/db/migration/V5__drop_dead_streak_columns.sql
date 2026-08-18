-- Bỏ 3 cột streak "chết" trên habits: chưa bao giờ được cập nhật (luôn 0/null) vì
-- streak được tính on-the-fly bằng StreakCalculator. Giữ chúng chỉ gây hiểu nhầm.
alter table habits drop column if exists current_streak;
alter table habits drop column if exists best_streak;
alter table habits drop column if exists last_checkin_date;
