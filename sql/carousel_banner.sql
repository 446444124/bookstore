-- 首页轮播图配置（商家端维护 / 用户端展示）
-- 表：carousel_banner

CREATE TABLE IF NOT EXISTS `carousel_banner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `image_url` VARCHAR(512) NOT NULL COMMENT '图片URL',
  `link_path` VARCHAR(255) NULL COMMENT '点击跳转路径（可选；为空仅展示图片）',
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '1=启用 0=停用',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序：越小越靠前',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` BIGINT NULL COMMENT '最后修改人（管理员/店员ID）',
  PRIMARY KEY (`id`),
  KEY `idx_banner_enabled_sort` (`enabled`, `sort`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页轮播图配置';

