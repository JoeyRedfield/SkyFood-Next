package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * AI营销文案生成请求DTO
 */
@Data
public class AiMarketingDTO implements Serializable {
    
    /**
     * 菜品名称
     */
    private String dishName;
    
    /**
     * 菜品特点
     */
    private String features;
    
    /**
     * 指定使用的AI提供商
     */
    private String provider;
    
    /**
     * 营销场景（促销、新品推广、节日营销等）
     */
    private String scenario;
    
    /**
     * 目标客户群体
     */
    private String targetAudience;
    
    /**
     * 文案长度要求
     */
    private Integer maxLength;
}
