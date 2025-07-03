package com.sky.constant;

/**
 * AI功能相关常量
 */
public class AiConstant {
    
    /**
     * AI模型提供商
     */
    public static final String PROVIDER_ZHIPUAI = "zhipuai";
    public static final String PROVIDER_OPENAI = "openai";
    public static final String PROVIDER_DASHSCOPE = "dashscope";
    public static final String PROVIDER_QIANFAN = "qianfan";
    
    /**
     * 默认AI模型
     */
    public static final String DEFAULT_PROVIDER = PROVIDER_ZHIPUAI;
    
    /**
     * AI功能类型
     */
    public static final String FUNCTION_CHAT = "chat";
    public static final String FUNCTION_RECOMMEND = "recommend";
    public static final String FUNCTION_CUSTOMER_SERVICE = "customer_service";
    public static final String FUNCTION_MARKETING = "marketing";
    
    /**
     * AI响应状态
     */
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";
    public static final String STATUS_TIMEOUT = "timeout";
}
