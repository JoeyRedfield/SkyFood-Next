package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI聊天响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    //会话ID
    private String sessionId;

    //助手回复内容
    private String reply;

    //响应时间
    private LocalDateTime responseTime;

    //消息数量
    private Integer messageCount;
}
