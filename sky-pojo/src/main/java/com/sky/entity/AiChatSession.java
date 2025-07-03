package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI聊天会话实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //用户ID
    private Long userId;

    //会话ID
    private String sessionId;

    //会话标题
    private String title;

    //创建时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;

    //是否删除 0未删除 1已删除
    private Integer isDeleted;
}
