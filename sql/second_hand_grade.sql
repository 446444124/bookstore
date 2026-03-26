-- 二手书成色档位配置（可自定义）
-- recycle_percent：回收百分比，相对店内原价
-- 执行前请备份数据库

CREATE TABLE IF NOT EXISTS second_hand_grade (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  recycle_percent DECIMAL(6,2) NOT NULL COMMENT '回收百分比（0-100）',
  enabled TINYINT NOT NULL DEFAULT 1,
  sort INT NOT NULL DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_second_hand_grade_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二手书成色档位（回收比例）';

-- 默认 4 档（可按需在后台调整）
INSERT IGNORE INTO second_hand_grade (id, name, recycle_percent, enabled, sort) VALUES
  (1, '近新', 78.00, 1, 10),
  (2, '良好', 62.00, 1, 20),
  (3, '一般', 48.00, 1, 30),
  (4, '较差', 32.00, 1, 40);

