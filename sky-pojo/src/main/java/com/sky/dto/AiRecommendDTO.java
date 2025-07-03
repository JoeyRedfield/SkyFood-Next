package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * AI菜品推荐请求DTO
 */
@Data
public class AiRecommendDTO implements Serializable {
    
    /**
     * 用户偏好
     */
    private String preference;
    
    /**
     * 预算范围
     */
    private String budget;
    
    /**
     * 指定使用的AI提供商
     */
    private String provider;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 地理位置信息
     */
    private String location;
    
    /**
     * 餐饮类型（早餐、午餐、晚餐、夜宵）
     */
    private String mealType;
}
