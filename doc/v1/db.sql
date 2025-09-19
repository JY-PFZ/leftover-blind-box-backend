-- railway.admins definition

CREATE TABLE `admins` (
                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                          `username` varchar(50) NOT NULL COMMENT '用户名',
                          `password_hash` varchar(255) NOT NULL COMMENT '密码哈希',
                          `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员表';


-- railway.magic_bags definition

CREATE TABLE `magic_bags` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              `merchant_id` bigint NOT NULL COMMENT '所属商家ID',
                              `title` varchar(100) NOT NULL COMMENT '标题，如“今日面包盲盒”',
                              `description` text COMMENT '描述（可选）',
                              `price` decimal(8,2) NOT NULL COMMENT '价格（元）',
                              `quantity` int NOT NULL COMMENT '库存数量',
                              `pickup_start_time` time NOT NULL COMMENT '可自提开始时间（如 18:00）',
                              `pickup_end_time` time NOT NULL COMMENT '可自提结束时间（如 20:00）',
                              `available_date` date NOT NULL COMMENT '有效日期（即当天）',
                              `category` varchar(50) DEFAULT NULL COMMENT '食物类型标签，如 面包、咖啡、便当',
                              `image_url` varchar(255) DEFAULT NULL COMMENT '图片URL',
                              `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否上架',
                              `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_merchant_date_title` (`merchant_id`,`available_date`,`title`),
                              KEY `magic_bags_merchant_id_IDX` (`merchant_id`,`available_date`) USING BTREE,
                              KEY `magic_bags_available_date_IDX` (`available_date`,`is_active`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='魔法袋表（临期食物盲盒）';


-- railway.merchants definition

CREATE TABLE `merchants` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `name` varchar(100) NOT NULL COMMENT '商家名称',
                             `phone` varchar(15) NOT NULL COMMENT '联系手机号',
                             `password_hash` varchar(255) NOT NULL COMMENT '密码哈希',
                             `business_license` varchar(255) NOT NULL COMMENT '营业执照图片URL',
                             `address` varchar(255) NOT NULL COMMENT '详细地址',
                             `latitude` decimal(10,8) DEFAULT NULL COMMENT '纬度（预留）',
                             `longitude` decimal(11,8) DEFAULT NULL COMMENT '经度（预留）',
                             `status` enum('pending','approved','rejected') NOT NULL DEFAULT 'pending' COMMENT '审核状态：待审核/通过/拒绝',
                             `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `approved_at` datetime DEFAULT NULL COMMENT '审核通过时间',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `phone` (`phone`),
                             KEY `merchants_status_IDX` (`status`) USING BTREE,
                             KEY `merchants_longitude_IDX` (`longitude`,`latitude`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家表';


-- railway.order_verifications definition

CREATE TABLE `order_verifications` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                       `order_id` bigint NOT NULL COMMENT '订单ID（逻辑关联）',
                                       `verified_by` bigint NOT NULL COMMENT '核销人（商家ID）',
                                       `verified_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '核销时间',
                                       `location` varchar(255) DEFAULT NULL COMMENT '核销地点（可选）',
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `order_verifications_unique` (`order_id`),
                                       KEY `order_verifications_verified_by_IDX` (`verified_by`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单核销记录表';


-- railway.orders definition

CREATE TABLE `orders` (
                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                          `order_no` varchar(32) NOT NULL COMMENT '订单编号',
                          `user_id` bigint NOT NULL COMMENT '用户ID（逻辑关联）',
                          `bag_id` bigint NOT NULL COMMENT '魔法袋ID（逻辑关联）',
                          `quantity` int NOT NULL DEFAULT '1' COMMENT '购买数量',
                          `total_price` decimal(8,2) NOT NULL COMMENT '总金额',
                          `status` enum('pending','paid','completed','cancelled') NOT NULL DEFAULT 'pending' COMMENT '订单状态',
                          `pickup_code` varchar(10) NOT NULL COMMENT '自提码',
                          `pickup_start_time` time NOT NULL COMMENT '可自提开始时间',
                          `pickup_end_time` time NOT NULL COMMENT '可自提结束时间',
                          `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
                          `paid_at` datetime DEFAULT NULL COMMENT '支付时间',
                          `completed_at` datetime DEFAULT NULL COMMENT '核销时间',
                          `cancelled_at` datetime DEFAULT NULL COMMENT '取消时间',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `order_no` (`order_no`),
                          KEY `orders_user_id_IDX` (`user_id`,`created_at`) USING BTREE,
                          KEY `orders_bag_id_IDX` (`bag_id`,`status`) USING BTREE,
                          KEY `orders_pickup_code_IDX` (`pickup_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';


-- railway.roles definition

CREATE TABLE `roles` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `name` varchar(100) NOT NULL,
                         `comment` varchar(100) DEFAULT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `roles_unique` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- railway.user_roles definition

CREATE TABLE `user_roles` (
                              `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
                              `user_id` int NOT NULL,
                              `role_id` int NOT NULL,
                              PRIMARY KEY (`id`),
                              KEY `user_roles_user_id_IDX` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- railway.users definition

CREATE TABLE `users` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'use email',
                         `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码哈希值',
                         `phone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号（唯一）',
                         `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
                         `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
                         `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                         `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         `user_roles` int DEFAULT NULL COMMENT '用户角色表',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `users_unique` (`username`),
                         UNIQUE KEY `phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表（消费者）';