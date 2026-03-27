-- 特惠专区：单品优惠 + 组合优惠

CREATE TABLE IF NOT EXISTS `special_offer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(80) NOT NULL COMMENT '活动名称',
  `offer_type` TINYINT NOT NULL COMMENT '1=单品 2=组合',
  `discount_type` TINYINT NOT NULL COMMENT '1=折扣(%) 2=一口价(元) 3=立减(元)',
  `discount_value` DECIMAL(10,2) NOT NULL COMMENT '折扣值/金额',
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '1=启用 0=停用',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序：越小越靠前',
  `start_time` DATETIME NULL COMMENT '开始时间(可空)',
  `end_time` DATETIME NULL COMMENT '结束时间(可空)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` BIGINT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_offer_enabled_sort` (`enabled`, `sort`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='特惠活动';

CREATE TABLE IF NOT EXISTS `special_offer_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `offer_id` BIGINT NOT NULL,
  `book_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '组合中该书数量；单品固定为1',
  PRIMARY KEY (`id`),
  KEY `idx_offer_item_offer` (`offer_id`),
  KEY `idx_offer_item_book` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='特惠活动包含图书';

-- orders 扩展：记录特惠来源与优惠金额（普通下单保持为空/0）
ALTER TABLE `orders`
  ADD COLUMN IF NOT EXISTS `special_offer_id` BIGINT NULL COMMENT '特惠活动ID' AFTER `second_hand_listing_id`,
  ADD COLUMN IF NOT EXISTS `discount_amount` DECIMAL(10,2) NULL COMMENT '优惠金额' AFTER `special_offer_id`;

