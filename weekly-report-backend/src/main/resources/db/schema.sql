-- 周报管理系统数据库脚本
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS weekly_report DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE weekly_report;

-- 部门表
CREATE TABLE sys_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB COMMENT='部门表';

-- 角色表
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(255) COMMENT '描述',
    permissions TEXT COMMENT '权限JSON',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用, 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_code (code)
) ENGINE=InnoDB COMMENT='角色表';

-- 用户表
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password VARCHAR(100) NOT NULL COMMENT '密码(MD5)',
    department_id BIGINT COMMENT '部门ID',
    role_id BIGINT COMMENT '角色ID',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用, 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_email (email),
    INDEX idx_department_id (department_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB COMMENT='用户表';

-- 周报表
CREATE TABLE weekly_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    week_number INT NOT NULL COMMENT '周次',
    year INT NOT NULL COMMENT '年份',
    this_week_work TEXT COMMENT '本周工作',
    next_week_plan TEXT COMMENT '下周计划',
    coordination TEXT COMMENT '需协调事项',
    status TINYINT DEFAULT 0 COMMENT '状态 0-草稿, 1-已提交',
    score DECIMAL(3,1) COMMENT '评分',
    comment TEXT COMMENT '评语',
    reviewer_id BIGINT COMMENT '评分人ID',
    submit_time DATETIME COMMENT '提交时间',
    review_time DATETIME COMMENT '评价时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_year_week (year, week_number),
    UNIQUE KEY uk_user_week (user_id, year, week_number)
) ENGINE=InnoDB COMMENT='周报表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '操作人ID',
    username VARCHAR(50) COMMENT '操作人姓名',
    module VARCHAR(50) COMMENT '模块',
    operation VARCHAR(100) COMMENT '操作描述',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(500) COMMENT '请求URL',
    ip VARCHAR(50) COMMENT 'IP地址',
    duration_ms BIGINT COMMENT '耗时毫秒',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB COMMENT='操作日志表';

-- 初始化数据

-- 部门数据
INSERT INTO sys_department (id, name, parent_id, sort) VALUES
(1, 'XX科技公司', 0, 1),
(2, '研发部', 1, 1),
(3, '后端组', 2, 1),
(4, '前端组', 2, 2),
(5, '产品部', 1, 2),
(6, '市场部', 1, 3);

-- 角色数据
INSERT INTO sys_role (id, name, code, description, permissions) VALUES
(1, '管理员', 'ADMIN', '系统超级管理员，拥有所有权限', '["*"]'),
(2, '组长', 'LEADER', '团队组长，可查看下属周报并评分', '["report:view","report:write","report:submit","team:view","team:review"]'),
(3, '员工', 'STAFF', '普通员工，可填写和提交周报', '["report:view","report:write","report:submit"]');

-- 用户数据 (密码: 123456 -> MD5: e10adc3949ba59abbe56e057f20f883e)
INSERT INTO sys_user (id, username, email, password, department_id, role_id, status) VALUES
(1, '管理员', 'admin@company.com', 'e10adc3949ba59abbe56e057f20f883e', NULL, 1, 1),
(2, '张三', 'zhangsan@company.com', 'e10adc3949ba59abbe56e057f20f883e', 3, 3, 1),
(3, '李四', 'lisi@company.com', 'e10adc3949ba59abbe56e057f20f883e', 3, 2, 1),
(4, '王五', 'wangwu@company.com', 'e10adc3949ba59abbe56e057f20f883e', 4, 3, 1),
(5, '赵六', 'zhaoliu@company.com', 'e10adc3949ba59abbe56e057f20f883e', 4, 3, 1);

-- 周报示例数据
INSERT INTO weekly_report (user_id, week_number, year, this_week_work, next_week_plan, coordination, status, score, comment, submit_time) VALUES
(2, 2, 2024, '1. 完成用户模块API开发\n2. 修复登录页面BUG\n3. 参与代码评审会议', '1. 完成订单模块开发\n2. 编写单元测试\n3. 优化数据库查询性能', '需产品经理确认订单流程细节', 1, 4.5, '工作完成度高，继续保持！', '2024-01-12 17:30:00'),
(3, 2, 2024, '1. 完成代码评审\n2. 优化API性能\n3. 编写技术文档', '1. 推进新功能开发\n2. 组织技术分享会', NULL, 1, 5.0, '非常出色！', '2024-01-12 16:20:00');
