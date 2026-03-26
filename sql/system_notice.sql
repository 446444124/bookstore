CREATE TABLE IF NOT EXISTS `system_notice` (
  `id` INT NOT NULL,
  `title` VARCHAR(128) NULL,
  `content` TEXT NULL,
  `enabled` TINYINT(1) NOT NULL DEFAULT 0,
  `update_time` DATETIME NULL,
  `update_by` BIGINT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化一条配置行（与 second_hand_config 同风格：固定 id=1）
INSERT INTO `system_notice` (`id`, `title`, `content`, `enabled`, `update_time`, `update_by`)
SELECT 1, '系统公告', '欢迎光临！', 0, NOW(), NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `system_notice` WHERE `id` = 1);

