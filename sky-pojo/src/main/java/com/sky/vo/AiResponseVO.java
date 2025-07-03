package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResponseVO implements Serializable {
    
    /**
     * 响应内容
     */
    private String content;
    
    /**
     * 使用的AI提供商
     */
    private String provider;
    
    /**
     * 响应状态
     */
    private String status;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 处理耗时（毫秒）
     */
    private Long processingTime;
    
    /**
     * 响应时间
     */
    private LocalDateTime responseTime;
    
    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
    
    /**
     * 使用的模型
     */
    private String model;
    
    /**
     * token使用情况
     */
    private TokenUsage tokenUsage;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsage {
        /**
         * 输入token数
         */
        private Integer promptTokens;
        
        /**
         * 输出token数
         */
        private Integer completionTokens;
        
        /**
         * 总token数
         */
        private Integer totalTokens;
    }
}
