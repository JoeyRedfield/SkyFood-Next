package com.sky.agent.constant;

/**
 * AI智能体相关常量定义
 * 
 * @author wuzy
 * @since 2025-07-14
 */
public class AgentConstants {
    
    /**
     * 默认配置常量
     */
    public static final int DEFAULT_MAX_STEPS = 10;              // 默认最大执行步骤数
    public static final long DEFAULT_TIMEOUT_MS = 300000L;       // 默认超时时间：5分钟
    public static final int DEFAULT_MAX_MESSAGES = 20;           // 默认最大消息数
    public static final long DEFAULT_SSE_TIMEOUT_MS = 300000L;   // SSE默认超时时间：5分钟
    
    /**
     * 系统提示词模板
     */
    public static final String CUSTOMER_SERVICE_SYSTEM_PROMPT = """
        你是苍穹外卖的专业AI客服助手，名字叫"小苍"。你的职责是：
        
        1. 友好、专业地回答用户关于外卖、菜品、订单等相关问题
        2. 根据用户需求提供准确的信息和建议
        3. 在需要时主动调用工具获取实时数据
        4. 保持礼貌、耐心的服务态度
        5. 如果遇到无法解决的问题，及时转接人工客服
        
        可用工具包括：
        - 订单查询：根据订单号查询订单状态和详情
        - 菜品推荐：根据用户喜好推荐合适的菜品
        - 营业查询：查询店铺营业时间和状态
        - 常见问题：快速解答常见问题
        
        请始终以用户体验为中心，提供高质量的客服服务。
        """;
    
    /**
     * ReAct 提示词模板
     */
    public static final String REACT_NEXT_STEP_PROMPT = """
        基于当前对话历史和用户需求，请按照以下格式进行思考和行动：
        
        思考: [分析用户的需求，判断需要采取什么行动]
        行动: [如果需要调用工具，说明调用哪个工具；如果可以直接回复，提供回复内容]
        
        请确保你的思考过程清晰，行动选择合理。
        """;
    
    /**
     * 工具调用相关常量
     */
    public static final String TOOL_CALL_PREFIX = "工具调用: ";
    public static final String TOOL_RESULT_PREFIX = "工具结果: ";
    public static final String THINKING_PREFIX = "思考: ";
    public static final String ACTION_PREFIX = "行动: ";
    
    /**
     * 错误消息模板
     */
    public static final String ERROR_MAX_STEPS_EXCEEDED = "对不起，当前任务步骤过多，请简化您的需求或稍后再试。";
    public static final String ERROR_TIMEOUT = "对不起，处理您的请求超时了，请稍后再试。";
    public static final String ERROR_TOOL_CALL_FAILED = "对不起，获取信息时出现问题，请稍后再试或联系人工客服。";
    public static final String ERROR_GENERAL = "对不起，处理您的请求时出现了问题，请稍后再试。";
    
    /**
     * 成功消息模板
     */
    public static final String SUCCESS_TASK_COMPLETED = "任务已完成";
    public static final String SUCCESS_TOOL_CALLED = "工具调用成功";
    
    /**
     * 文件存储相关常量
     */
    public static final String MEMORY_BASE_DIR = "data/memory";
    public static final String MEMORY_FILE_EXTENSION = ".dat";
    public static final String RAG_BASE_DIR = "data/rag";
    public static final String RAG_DOCS_DIR = "docs";
    
    /**
     * 私有构造函数，防止实例化
     */
    private AgentConstants() {
        throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }
}
