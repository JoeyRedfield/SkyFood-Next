-- 创建AI聊天会话表
CREATE TABLE `ai_chat_session` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `session_id` varchar(64) NOT NULL COMMENT '会话ID',
    `title` varchar(255) NOT NULL COMMENT '会话标题',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_id` (`session_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天会话表';

-- 创建AI聊天消息表
CREATE TABLE `ai_chat_message` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `session_id` varchar(64) NOT NULL COMMENT '会话ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `message_type` varchar(20) NOT NULL COMMENT '消息类型 user用户消息 assistant助手消息',
    `content` text NOT NULL COMMENT '消息内容',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天消息表';

-- 创建Spring AI聊天记忆表
CREATE TABLE `chat_memory` (
    `id` varchar(255) NOT NULL COMMENT '主键',
    `conversation_id` varchar(255) NOT NULL COMMENT '对话ID',
    `create_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `content` json NOT NULL COMMENT '消息内容(JSON格式)',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_create_at` (`create_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring AI聊天记忆表';

-- 插入一些测试数据（可选）
INSERT INTO `ai_chat_session` (`user_id`, `session_id`, `title`, `create_time`, `update_time`, `is_deleted`) VALUES
(1, 'test-session-001', '测试会话', NOW(), NOW(), 0);

INSERT INTO `ai_chat_message` (`session_id`, `user_id`, `message_type`, `content`, `create_time`) VALUES
('test-session-001', 1, 'user', '你好，我想了解一下菜品信息', NOW()),
('test-session-001', 1, 'assistant', '您好！我是苍穹外卖的AI客服，很高兴为您服务。请问您想了解哪个菜品的信息呢？', NOW());
