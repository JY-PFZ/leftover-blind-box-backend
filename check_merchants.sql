-- 检查 merchants 表结构和数据

-- 1. 查看 merchants 表结构
DESCRIBE merchants;

-- 2. 查看 merchants 表中的所有记录
SELECT * FROM merchants;

-- 3. 特别检查是否有 user_id = 19 的记录
SELECT * FROM merchants WHERE user_id = 19;


