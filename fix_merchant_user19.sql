-- 为 merchants 表添加 user_id 字段并创建商家记录
-- 这是必需的，因为后端代码通过 user_id 查找 merchant

-- 1. 添加 user_id 字段到 merchants 表
ALTER TABLE merchants ADD COLUMN user_id bigint COMMENT '关联的用户ID';

-- 2. 添加 score 字段（如果还没有）
ALTER TABLE merchants ADD COLUMN score decimal(3,2) DEFAULT 0.0 COMMENT '商家评分';

-- 3. 为用户 19 创建商家记录
INSERT INTO merchants (
    user_id,
    name,
    phone,
    business_license,
    address,
    latitude,
    longitude,
    status,
    score,
    created_at,
    updated_at
) VALUES (
    19,  -- user_id (关联到 users.id)
    'Test Merchant Store',  -- name
    '12345678901',  -- phone
    'https://example.com/license.jpg',  -- business_license
    '123 Test Street, Test City',  -- address
    1.3521,  -- latitude (Singapore)
    103.8198,  -- longitude (Singapore)
    'approved',  -- status (必须是 approved 才能正常使用)
    4.5,  -- score
    NOW(),  -- created_at
    NOW()   -- updated_at
);

-- 4. 验证记录已创建
SELECT * FROM merchants WHERE user_id = 19;
