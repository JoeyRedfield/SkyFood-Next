package com.sky.agent.core;

import com.sky.agent.constant.AgentConstants;
import com.sky.agent.constant.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * ReAct (Reasoning and Acting) 智能体抽象类
 * 实现"推理-行动"循环模式
 * 
 * ReAct模式核心思想：
 * 1. Think（思考）：分析当前情况，决定下一步行动
 * 2. Act（行动）：执行具体的行动（工具调用或直接回复）
 * 3. 循环执行直到任务完成
 * 
 * 特点：
 * - 模拟人类问题解决过程
 * - 透明的推理过程
 * - 自主决策能力
 * - 错误处理和纠正
 * 
 * @author wuzy
 * @since 2025-07-14
 */
@Slf4j
public abstract class ReActAgent extends BaseAgent {
    
    /**
     * 构造函数
     * @param name 智能体名称
     * @param systemPrompt 系统提示词
     * @param chatModel 聊天模型
     */
    protected ReActAgent(String name, String systemPrompt, ChatModel chatModel) {
        super(name, systemPrompt, chatModel);
        log.info("ReAct智能体 [{}] 初始化完成", name);
    }
    
    // ============== 抽象方法 ==============
    
    /**
     * 思考阶段
     * 分析当前情况，决定是否需要采取行动
     * 
     * @return true表示需要执行行动，false表示可以直接响应
     */
    protected abstract boolean think();
    
    /**
     * 行动阶段
     * 执行具体的行动（工具调用或直接回复）
     * 
     * @return 行动执行结果
     */
    protected abstract String act();
    
    // ============== 核心实现 ==============
    
    /**
     * 实现BaseAgent的step方法
     * 按照ReAct模式执行思考-行动循环
     */
    @Override
    protected String step() {
        try {
            log.debug("ReAct智能体 [{}] 开始第 {} 步执行", getName(), getCurrentStep());
            
            // 1. 思考阶段
            setState(AgentState.THINKING);
            log.debug("进入思考阶段...");
            
            boolean needAction = think();
            
            // 2. 如果不需要行动，直接生成回复
            if (!needAction) {
                log.debug("思考结果：不需要工具调用，直接生成回复");
                return generateDirectResponse();
            }
            
            // 3. 行动阶段
            setState(AgentState.ACTING);
            log.debug("进入行动阶段...");
            
            String actionResult = act();
            
            // 4. 处理行动结果
            if (actionResult != null && !actionResult.trim().isEmpty()) {
                log.debug("行动执行完成，结果: {}", actionResult);
                return actionResult;
            }
            
            // 5. 如果行动没有产生最终结果，继续下一步
            log.debug("行动已执行，但未产生最终结果，继续下一步");
            return null;
            
        } catch (Exception e) {
            log.error("ReAct智能体 [{}] 第 {} 步执行失败", getName(), getCurrentStep(), e);
            setState(AgentState.ERROR);
            throw new RuntimeException("ReAct步骤执行失败", e);
        }
    }
    
    /**
     * 生成直接回复
     * 当不需要工具调用时，直接与AI模型对话生成回复
     * 
     * @return AI生成的回复
     */
    protected String generateDirectResponse() {
        try {
            log.debug("生成直接回复...");
            
            // 构建提示词，包含对话历史
            Prompt prompt = new Prompt(getMessageHistory());
            
            // 调用AI模型生成回复
            ChatResponse response = getChatModel().call(prompt);
            
            if (response == null || response.getResult() == null) {
                log.warn("AI模型返回空响应");
                return "抱歉，我现在无法理解您的问题，请稍后再试。";
            }
            
            String content = response.getResult().getOutput().getText();
            
            // 添加到消息历史
            addAssistantMessage(content);
            
            log.debug("直接回复生成完成: {}", content);
            return content;
            
        } catch (Exception e) {
            log.error("生成直接回复失败", e);
            return AgentConstants.ERROR_GENERAL;
        }
    }
    
    /**
     * 辅助方法：发送思考提示给AI模型
     * 
     * @param context 当前上下文
     * @return AI的思考结果
     */
    protected String sendThinkingPrompt(String context) {
        try {
            // 构建思考提示词
            String thinkingPrompt = String.format("""
                %s
                
                当前上下文：%s
                
                请分析用户的需求并思考：
                1. 用户想要什么？
                2. 我是否需要调用工具来获取信息？
                3. 如果需要调用工具，应该调用哪个工具？
                4. 如果不需要调用工具，我可以直接回答吗？
                
                请给出你的思考过程和决策。
                """, getNextStepPrompt(), context);
            
            // 创建临时消息列表用于思考
            var thinkingMessages = getMessageHistory();
            thinkingMessages.add(new org.springframework.ai.chat.messages.UserMessage(thinkingPrompt));
            
            Prompt prompt = new Prompt(thinkingMessages);
            ChatResponse response = getChatModel().call(prompt);
            
            if (response != null && response.getResult() != null) {
                String thinking = response.getResult().getOutput().getText();
                log.debug("AI思考结果: {}", thinking);
                return thinking;
            }
            
            return "";
            
        } catch (Exception e) {
            log.error("发送思考提示失败", e);
            return "";
        }
    }
    
    /**
     * 解析思考结果，判断是否需要工具调用
     * 
     * @param thinking 思考内容
     * @return true表示需要工具调用
     */
    protected boolean parseThinkingResult(String thinking) {
        if (thinking == null || thinking.trim().isEmpty()) {
            return false;
        }
        
        // 简单的关键词匹配，判断是否需要工具调用
        String lowerThinking = thinking.toLowerCase();
        
        // 工具调用关键词
        String[] toolKeywords = {
            "调用工具", "查询", "搜索", "获取", "工具", 
            "订单", "菜品", "营业", "状态", "信息"
        };
        
        for (String keyword : toolKeywords) {
            if (lowerThinking.contains(keyword)) {
                log.debug("思考结果包含工具调用关键词: {}", keyword);
                return true;
            }
        }
        
        // 直接回复关键词
        String[] directKeywords = {
            "直接回答", "不需要", "可以回答", "已知", "直接回复"
        };
        
        for (String keyword : directKeywords) {
            if (lowerThinking.contains(keyword)) {
                log.debug("思考结果包含直接回复关键词: {}", keyword);
                return false;
            }
        }
        
        // 默认不需要工具调用
        return false;
    }
    
    /**
     * 获取当前对话上下文
     * 
     * @return 格式化的对话上下文
     */
    protected String getCurrentContext() {
        var messages = getMessageHistory();
        if (messages.isEmpty()) {
            return "暂无对话历史";
        }
        
        StringBuilder context = new StringBuilder();
        context.append("对话历史：\n");
        
        for (Message message : messages) {
            String role = message instanceof org.springframework.ai.chat.messages.UserMessage ? "用户" : "助手";
            context.append(String.format("%s: %s\n", role, message.getText()));
        }
        
        return context.toString();
    }
    
    /**
     * 记录思考过程
     * 
     * @param thinking 思考内容
     */
    protected void logThinking(String thinking) {
        log.info("{}[{}] {}", AgentConstants.THINKING_PREFIX, getName(), thinking);
    }
    
    /**
     * 记录行动过程
     * 
     * @param action 行动内容
     */
    protected void logAction(String action) {
        log.info("{}[{}] {}", AgentConstants.ACTION_PREFIX, getName(), action);
    }
}
