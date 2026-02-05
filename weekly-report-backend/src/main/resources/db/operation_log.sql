-- 操作日志表（若已建库，单独执行本脚本即可）
-- MySQL 8.0+

USE weekly_report;

CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '操作人ID',
    username VARCHAR(50) COMMENT '操作人姓名',
    module VARCHAR(50) COMMENT '模块（如：用户管理）',
    operation VARCHAR(100) COMMENT '操作描述（如：新增用户）',
    request_method VARCHAR(10) COMMENT '请求方法 GET/POST等',
    request_url VARCHAR(500) COMMENT '请求URL',
    ip VARCHAR(50) COMMENT 'IP地址',
    duration_ms BIGINT COMMENT '耗时毫秒',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB COMMENT='操作日志表';
