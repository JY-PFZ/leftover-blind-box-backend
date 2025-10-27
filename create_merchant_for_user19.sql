-- 为用户 19 创建 merchant 记录
-- 用户信息: merchanttest@123456.com, ID: 19

-- 1. 检查 merchants 表是否有 user_id 字段
-- 如果没有，需要先添加这个字段

-- 2. 为用户 19 创建 merchant 记录
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
    19,  -- user_id
    'Test Merchant Store',  -- name
    '12345678901',  -- phone
    'https://example.com/license.jpg',  -- business_license
    '123 Test Street, Test City',  -- address
    1.3521,  -- latitude (Singapore)
    103.8198,  -- longitude (Singapore)
    'approved',  -- status
    4.5,  -- score
    NOW(),  -- created_at
    NOW()   -- updated_at
);

-- 3. 验证记录已创建
SELECT * FROM merchants WHERE user_id = 19;


