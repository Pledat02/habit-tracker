-- Chống TRÙNG thành tựu account-level (habit_id NULL). Unique constraint thường
-- (user_id, definition_id, habit_id) KHÔNG bắt được vì trong SQL, NULL != NULL nên
-- nhiều dòng (user_id, definition_id, NULL) đều được coi là khác nhau -> lọt.

-- 1) Dọn trùng cũ nếu có (giữ dòng cũ nhất theo id) — để bước tạo unique index không lỗi.
DELETE FROM user_achivements a
USING user_achivements b
WHERE a.habit_id IS NULL
  AND b.habit_id IS NULL
  AND a.user_id = b.user_id
  AND a.definition_id = b.definition_id
  AND a.id > b.id;

-- 2) Partial unique index: chỉ áp cho dòng account-level, ép (user_id, definition_id) duy nhất.
CREATE UNIQUE INDEX uq_user_ach_account
    ON user_achivements (user_id, definition_id)
    WHERE habit_id IS NULL;
