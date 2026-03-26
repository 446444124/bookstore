-- 二手书条目扩展字段：支持档位配置与中间商回收价/服务费快照
-- 执行前请备份数据库

ALTER TABLE second_hand_listing
  ADD COLUMN grade_id BIGINT NULL COMMENT '成色档位ID（second_hand_grade）' AFTER condition_grade,
  ADD COLUMN grade_name VARCHAR(64) NULL COMMENT '成色档位名称快照' AFTER grade_id,
  ADD COLUMN recycle_price DECIMAL(10,2) NULL COMMENT '平台回收打款金额快照' AFTER ref_book_price,
  ADD COLUMN service_fee_percent DECIMAL(6,2) NULL COMMENT '服务费百分比快照（用于售价）' AFTER recycle_price;

