-- 二手书回收配置（中间商模式）
-- service_fee_percent：服务费百分比，用于售价 = 回收价 * (1 + 服务费%)
-- 执行前请备份数据库

CREATE TABLE IF NOT EXISTS second_hand_config (
  id INT PRIMARY KEY,
  service_fee_percent DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '服务费百分比（0-100）',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_by BIGINT DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二手书回收配置（全局）';

-- 初始化一行配置（若已存在则忽略）
INSERT IGNORE INTO second_hand_config (id, service_fee_percent) VALUES (1, 0.00);

