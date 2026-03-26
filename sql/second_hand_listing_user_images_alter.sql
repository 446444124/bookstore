-- 已有 second_hand_listing 表、需补「用户实拍」列时执行（执行一次即可）
ALTER TABLE second_hand_listing
  ADD COLUMN user_condition_images TEXT NULL COMMENT '用户上传成色参考图 URL 列表 JSON' AFTER user_note;
