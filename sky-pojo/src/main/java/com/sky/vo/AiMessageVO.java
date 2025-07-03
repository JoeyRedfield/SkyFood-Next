package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI消息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    //消息类型 user用户消息 assistant助手消息
    private String messageType;

    //消息内容
    private String content;

    //创建时间
    private LocalDateTime createTime;
}
