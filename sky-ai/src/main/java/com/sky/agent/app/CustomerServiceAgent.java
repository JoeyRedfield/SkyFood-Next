package com.sky.agent.app;

import com.sky.agent.constant.AgentConstants;
import com.sky.agent.core.ToolCallAgent;
import com.sky.agent.tool.AgentTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 苍穹外卖AI客服智能体
 * 专门为外卖业务场景设计的智能客服代理
 * 
 * 核心功能：
 * 1. 订单查询和状态跟踪
 * 2. 菜品推荐和信息查询
 * 3. 营业时间和店铺状态查询
 * 4. 常见问题自动回答
 * 5. 客户投诉处理
 * 6. 优惠活动咨询
 * 
 * 特色：
 * - 友好专业的服务态度
 * - 准确快速的信息查询
 * - 主动的问题解决
 * - 智能的需求理解
 * 
 * @author wuzy
 * @since 2025-07-14
 */
@Slf4j
@Component
public class CustomerServiceAgent extends ToolCallAgent {
    
    /**
     * 客服智能体名称
     */
    private static final String AGENT_NAME = "苍穹外卖AI客服小苍";
    
    /**
     * 构造函数
     * @param chatModel 聊天模型
     * @param tools 工具列表
     */
    public CustomerServiceAgent(ChatModel chatModel, List<AgentTool> tools) {
        super(AGENT_NAME, AgentConstants.CUSTOMER_SERVICE_SYSTEM_PROMPT, chatModel, tools);
        log.info("苍穹外卖AI客服智能体初始化完成");
    }
    
    /**
     * 默认构造函数（无工具）
     * @param chatModel 聊天模型
     */
    public CustomerServiceAgent(ChatModel chatModel) {
        super(AGENT_NAME, AgentConstants.CUSTOMER_SERVICE_SYSTEM_PROMPT, chatModel);
        log.info("苍穹外卖AI客服智能体初始化完成（无预设工具）");
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        log.debug("客服智能体开始处理新的用户会话");
    }
    
    @Override
    protected void cleanup() {
        super.cleanup();
        log.debug("客服智能体会话处理完成");
    }
    
    /**
     * 处理客户咨询
     * @param userInput 用户输入
     * @param userId 用户ID
     * @return 客服回复
     */
    public String handleCustomerInquiry(String userInput, String userId) {
        log.info("客服智能体接收到用户 [{}] 的咨询：{}", userId, userInput);
        
        try {
            // 预处理用户输入
            String processedInput = preprocessUserInput(userInput);
            
            // 执行智能体处理
            String response = run(processedInput, userId);
            
            log.info("客服智能体为用户 [{}] 生成回复：{}", userId, response);
            return response;
            
        } catch (Exception e) {
            log.error("处理用户 [{}] 咨询时发生异常：{}", userId, userInput, e);
            return "抱歉，我现在遇到了一些技术问题。请稍后再试，或者联系人工客服：400-8888-888。";
        }
    }
    
    /**
     * 预处理用户输入
     * 对用户输入进行标准化和优化
     * 
     * @param userInput 原始用户输入
     * @return 处理后的输入
     */
    private String preprocessUserInput(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return "用户发送了空消息";
        }
        
        String processed = userInput.trim();
        
        // 添加礼貌性前缀（如果用户输入比较简短且没有礼貌用语）
        if (processed.length() < 10 && !containsPoliteWords(processed)) {
            processed = "请问" + processed;
        }
        
        return processed;
    }
    
    /**
     * 检查是否包含礼貌用语
     * @param input 输入文本
     * @return true表示包含礼貌用语
     */
    private boolean containsPoliteWords(String input) {
        String[] politeWords = {"请", "您好", "谢谢", "麻烦", "请问", "劳烦"};
        String lowerInput = input.toLowerCase();
        
        for (String word : politeWords) {
            if (lowerInput.contains(word)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 快速回复常见问题
     * 对于一些简单的常见问题，直接给出预设回复
     * 
     * @param userInput 用户输入
     * @return 快速回复内容，如果不是常见问题返回null
     */
    public String getQuickReply(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return null;
        }
        
        String input = userInput.toLowerCase().trim();
        
        // 打招呼
        if (input.matches(".*[你您]好.*|.*hi.*|.*hello.*")) {
            return "您好！欢迎来到苍穹外卖，我是您的专属AI客服小苍。请问有什么可以帮助您的吗？";
        }
        
        // 营业时间询问
        if (input.contains("营业时间") || input.contains("几点") && input.contains("营业")) {
            return "我们的营业时间是每天上午10:00至晚上22:00。如需查询具体门店信息，我可以帮您查询。";
        }
        
        // 联系方式
        if (input.contains("电话") || input.contains("联系") || input.contains("客服")) {
            return "我们的客服热线是：400-8888-888，服务时间：9:00-21:00。我是AI客服小苍，也可以为您提供帮助哦！";
        }
        
        // 配送范围
        if (input.contains("配送") && (input.contains("范围") || input.contains("地区"))) {
            return "我们的配送覆盖市区大部分地区，具体可配送范围请提供您的详细地址，我来帮您核实。";
        }
        
        // 配送费用
        if (input.contains("配送费") || (input.contains("配送") && input.contains("费"))) {
            return "配送费根据距离计算，一般在2-8元之间。满39元免配送费哦！";
        }
        
        return null; // 不是常见问题，需要进一步处理
    }
    
    /**
     * 获取客服智能体状态报告
     * @return 状态报告
     */
    public String getStatusReport() {
        return String.format("""
                === 苍穹外卖AI客服智能体状态报告 ===
                智能体名称：%s
                当前状态：%s
                可用工具数量：%d
                工具调用统计：%s
                
                我是小苍，随时为您提供优质的外卖服务！
                """, 
                getName(), 
                getState().getDescription(),
                getToolManager().getToolCount(),
                getToolCallStats());
    }
    
    /**
     * 获取工具管理器（提供给外部访问）
     * @return 工具管理器
     */
    public com.sky.agent.tool.ToolManager getToolManager() {
        return com.sky.agent.tool.ToolManager.getInstance();
    }
}
