-- ========================================
-- 更新用户角色枚举
-- ========================================

USE county_risk_warning_system;

-- 第一步：先扩展ENUM，包含所有新旧值
ALTER TABLE users 
MODIFY COLUMN role ENUM(
    'ADMIN',           -- 系统管理员：全部权限
    'USER',            -- 普通用户：只能看风险监控大屏
    'VIP',             -- VIP用户：USER权限 + AI助手
    'RISK_ANALYST',    -- 风险分析员：VIP权限 + 数据增删改 + 风险参数调整 + 全部页面
    'DATA_MAINTAINER', -- 数据维护员：VIP权限 + 数据增删改 + 全部页面
    -- 保留旧值以便迁移
    'EXPERT',
    'ANALYST',
    'VIEWER',
    'DATA_ENGINEER'
) NOT NULL DEFAULT 'USER';

-- 第二步：更新现有用户的角色
-- 将原来的 VIEWER 改为 USER
UPDATE users SET role = 'USER' WHERE role = 'VIEWER';

-- 将原来的 ANALYST 改为 RISK_ANALYST
UPDATE users SET role = 'RISK_ANALYST' WHERE role = 'ANALYST';

-- 将原来的 DATA_ENGINEER 改为 DATA_MAINTAINER
UPDATE users SET role = 'DATA_MAINTAINER' WHERE role = 'DATA_ENGINEER';

-- 将原来的 EXPERT 改为 RISK_ANALYST（如果没有对应的新角色，可以改为其他）
UPDATE users SET role = 'RISK_ANALYST' WHERE role = 'EXPERT';

-- 第三步：清理ENUM，只保留新值
ALTER TABLE users 
MODIFY COLUMN role ENUM(
    'ADMIN',           -- 系统管理员：全部权限
    'USER',            -- 普通用户：只能看风险监控大屏
    'VIP',             -- VIP用户：USER权限 + AI助手
    'RISK_ANALYST',    -- 风险分析员：VIP权限 + 数据增删改 + 风险参数调整 + 全部页面
    'DATA_MAINTAINER'  -- 数据维护员：VIP权限 + 数据增删改 + 全部页面
) NOT NULL DEFAULT 'USER';

-- 插入示例用户（如果需要）
INSERT INTO users (username, password_hash, role, real_name, email, department) VALUES
('user1', SHA2('user123', 256), 'USER', '普通用户', 'user1@example.com', '综合办公室'),
('vip1', SHA2('vip123', 256), 'VIP', 'VIP用户', 'vip1@example.com', '综合办公室'),
('analyst1', SHA2('analyst123', 256), 'RISK_ANALYST', '风险分析员', 'analyst1@example.com', '风险管理部'),
('maintainer1', SHA2('maintainer123', 256), 'DATA_MAINTAINER', '数据维护员', 'maintainer1@example.com', '信息技术部')
ON DUPLICATE KEY UPDATE role = VALUES(role);

SELECT '用户角色更新完成！' as message;

