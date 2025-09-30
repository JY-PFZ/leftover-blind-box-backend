-- 商品表
CREATE TABLE `products` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `merchant_id` bigint NOT NULL,
    `name` varchar(100) NOT NULL,
    `description` text,
    `price` decimal(8,2) NOT NULL,
    `stock` int NOT NULL DEFAULT 0,
    `category` varchar(50),
    `image_url` varchar(255),
    `is_active` tinyint(1) DEFAULT 1,
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_category` (`category`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品表';

-- 插入示例数据
INSERT INTO `products` (`merchant_id`, `name`, `description`, `price`, `stock`, `category`, `image_url`, `is_active`) VALUES
(1, 'Candy 1', 'Yummy candy.', 4.99, 100, 'Candy', NULL, 1),
(1, 'Candy 2', 'Yummy candy.', 4.99, 100, 'Candy', NULL, 1),
(1, 'Candy 3', 'Yummy candy.', 4.99, 100, 'Candy', NULL, 1),
(1, 'Choco 1', 'Dark chocolate bar.', 6.99, 50, 'Chocolate', NULL, 1),
(1, 'Choco 2', 'Milk chocolate bar.', 7.49, 50, 'Chocolate', NULL, 1),
(1, 'Fruit Snack 1', 'Dried mango.', 5.99, 80, 'Fruit', NULL, 1),
(1, 'Fruit Snack 2', 'Dried strawberry.', 5.49, 80, 'Fruit', NULL, 1);


