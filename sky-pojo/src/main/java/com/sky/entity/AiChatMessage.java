package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI聊天消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //会话ID
    private String sessionId;

    //用户ID
    private Long userId;

    //消息类型 user用户消息 assistant助手消息
    private String messageType;

    //消息内容
    private String content;

    //创建时间
    private LocalDateTime createTime;
}
