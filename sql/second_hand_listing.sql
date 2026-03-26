-- 二手书回收上架：用户提交 -> 店员评估成色与折价 -> 上架 -> 用户购买
-- 执行前请备份数据库

CREATE TABLE IF NOT EXISTS second_hand_listing (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  book_id BIGINT NOT NULL COMMENT '店内图书ID，须存在于 book 表',
  seller_user_id BIGINT NOT NULL COMMENT '提交用户',
  user_note VARCHAR(500) DEFAULT NULL COMMENT '用户备注',
  user_condition_images TEXT DEFAULT NULL COMMENT '用户上传成色参考图 URL 列表 JSON',
  condition_grade TINYINT DEFAULT NULL COMMENT '成色 1近新 2良好 3一般 4较差（店员评估）',
  price_ratio INT DEFAULT NULL COMMENT '相对店内原价折扣百分比 1-100',
  ref_book_price DECIMAL(10,2) DEFAULT NULL COMMENT '评估时店内原价快照',
  sale_price DECIMAL(10,2) DEFAULT NULL COMMENT '二手售价',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核 1驳回 2在售 3已售 4用户撤回 8待支付锁定',
  staff_remark VARCHAR(500) DEFAULT NULL,
  buyer_user_id BIGINT DEFAULT NULL,
  order_id VARCHAR(64) DEFAULT NULL COMMENT '成交订单号',
  pending_order_id VARCHAR(64) DEFAULT NULL COMMENT '未支付订单占用',
  sold_time DATETIME DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_book_id (book_id),
  INDEX idx_status (status),
  INDEX idx_seller (seller_user_id),
  INDEX idx_pending_order (pending_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户二手书回收条目';

ALTER TABLE orders ADD COLUMN second_hand_listing_id BIGINT NULL COMMENT '关联二手条目，下单时占用' AFTER delivery_way;

-- 若表已存在但无此列，可单独执行：
-- ALTER TABLE second_hand_listing ADD COLUMN user_condition_images TEXT NULL COMMENT '用户上传成色参考图 URL 列表 JSON' AFTER user_note;
