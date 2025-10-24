-- 购物车转订单功能数据库迁移脚本
-- 执行前请先备份数据库

-- 1. 在 orders 表中添加订单类型字段
ALTER TABLE `orders` 
ADD COLUMN `order_type` enum('single','cart') NOT NULL DEFAULT 'single' COMMENT '订单类型：single-单商品订单，cart-购物车订单';

-- 2. 创建订单明细表
CREATE TABLE `order_items` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` bigint NOT NULL COMMENT '订单ID',
    `magic_bag_id` bigint NOT NULL COMMENT '魔法袋ID',
    `quantity` int NOT NULL COMMENT '购买数量',
    `unit_price` decimal(8,2) NOT NULL COMMENT '单价',
    `subtotal` decimal(8,2) NOT NULL COMMENT '小计',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `order_items_order_id_IDX` (`order_id`),
    KEY `order_items_magic_bag_id_IDX` (`magic_bag_id`),
    CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_order_items_magic_bag` FOREIGN KEY (`magic_bag_id`) REFERENCES `magic_bags` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';

-- 3. 验证表结构
DESCRIBE `orders`;
DESCRIBE `order_items`;

-- 4. 验证外键约束
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE 
    REFERENCED_TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'order_items';
