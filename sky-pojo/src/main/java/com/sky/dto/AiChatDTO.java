package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * AI聊天DTO
 */
@Data
public class AiChatDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    //会话ID（可选，新对话时为空）
    private String sessionId;

    //用户消息内容
    private String message;

    //是否流式响应
    private Boolean stream = false;
    /*
     * 会话ID（用于上下文对话）
     */
//    private String sessionId;
    
    /**
     * 用户ID
     */
    private Long userId;
}
