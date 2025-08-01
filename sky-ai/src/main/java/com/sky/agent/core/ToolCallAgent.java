package com.sky.agent.core;

import com.sky.agent.constant.AgentConstants;
import com.sky.agent.tool.AgentTool;
import com.sky.agent.tool.ToolManager;
import com.sky.agent.tool.ToolResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具调用智能体
 * 基于ReAct模式，具备工具调用能力的智能体
 * 
 * 核心功能：
 * 1. 智能分析用户需求，决定是否需要调用工具
 * 2. 自动选择合适的工具进行调用
 * 3. 处理工具调用结果并生成用户友好的回复
 * 4. 支持多轮工具调用和复杂任务处理
 * 
 * @author wuzy
 * @since 2025-07-14
 */
@Slf4j
public class ToolCallAgent extends ReActAgent {
    
    /**
     * 工具管理器
     */
    private final ToolManager toolManager;
    
    /**
     * 最后一次工具调用结果
     */
    private ToolResult lastToolResult;
    
    /**
     * 工具调用计数器
     */
    private int toolCallCount = 0;
    
    /**
     * 工具调用模式的正则表达式
     * 匹配格式: 工具名称(参数)
     */
    private static final Pattern TOOL_CALL_PATTERN = 
            Pattern.compile("(\\w+)\\s*\\(([^)]*)\\)", Pattern.CASE_INSENSITIVE);
    
    /**
     * 构造函数
     * @param name 智能体名称
     * @param systemPrompt 系统提示词
     * @param chatModel 聊天模型
     * @param tools 可用工具列表
     */
    public ToolCallAgent(String name, String systemPrompt, ChatModel chatModel, List<AgentTool> tools) {
        super(name, systemPrompt, chatModel);
        this.toolManager = ToolManager.getInstance();
        
        // 注册工具
        if (tools != null && !tools.isEmpty()) {
            int registeredCount = toolManager.registerTools(tools);
            log.info("工具调用智能体 [{}] 初始化完成，注册了 {} 个工具", name, registeredCount);
        } else {
            log.info("工具调用智能体 [{}] 初始化完成，未注册任何工具", name);
        }
    }
    
    /**
     * 构造函数（使用默认工具管理器）
     * @param name 智能体名称
     * @param systemPrompt 系统提示词
     * @param chatModel 聊天模型
     */
    public ToolCallAgent(String name, String systemPrompt, ChatModel chatModel) {
        super(name, systemPrompt, chatModel);
        this.toolManager = ToolManager.getInstance();
        log.info("工具调用智能体 [{}] 初始化完成，使用全局工具管理器", name);
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        this.lastToolResult = null;
        this.toolCallCount = 0;
        log.debug("工具调用智能体 [{}] 初始化完成", getName());
    }
    
    @Override
    protected void cleanup() {
        super.cleanup();
        this.lastToolResult = null;
        this.toolCallCount = 0;
        log.debug("工具调用智能体 [{}] 清理完成", getName());
    }
    
    /**
     * 思考阶段实现
     * 分析当前情况，决定是否需要调用工具
     */
    @Override
    protected boolean think() {
        try {
            log.debug("开始思考阶段...");
            
            // 构建思考上下文
            String context = buildThinkingContext();
            
            // 发送思考提示给AI
            String thinking = sendThinkingPrompt(context);
            logThinking(thinking);
            
            // 解析思考结果
            boolean needToolCall = parseThinkingResult(thinking);
            
            log.debug("思考结果：{}", needToolCall ? "需要工具调用" : "直接回复");
            return needToolCall;
            
        } catch (Exception e) {
            log.error("思考阶段执行失败", e);
            return false; // 失败时默认不调用工具
        }
    }
    
    /**
     * 行动阶段实现
     * 执行工具调用或生成回复
     */
    @Override
    protected String act() {
        try {
            log.debug("开始行动阶段...");
            
            // 生成行动决策
            String actionDecision = generateActionDecision();
            logAction(actionDecision);
            
            // 解析并执行工具调用
            ToolResult toolResult = parseAndExecuteToolCall(actionDecision);
            
            if (toolResult != null) {
                this.lastToolResult = toolResult;
                this.toolCallCount++;
                
                // 将工具调用结果添加到消息历史
                String resultMessage = String.format("%s%s", 
                        AgentConstants.TOOL_RESULT_PREFIX, toolResult.getFormattedResult());
                addAssistantMessage(resultMessage);
                
                // 如果是最后一步或工具调用失败，生成最终回复
                if (getCurrentStep() >= getMaxSteps() - 1 || !toolResult.isSuccess()) {
                    return generateFinalResponse(toolResult);
                }
                
                // 继续下一步
                return null;
            } else {
                // 没有工具调用，生成直接回复
                return generateDirectResponse();
            }
            
        } catch (Exception e) {
            log.error("行动阶段执行失败", e);
            return AgentConstants.ERROR_TOOL_CALL_FAILED;
        }
    }
    
    /**
     * 构建思考上下文
     * @return 思考上下文字符串
     */
    private String buildThinkingContext() {
        StringBuilder context = new StringBuilder();
        
        // 添加对话历史
        context.append(getCurrentContext()).append("\n");
        
        // 添加可用工具信息
        context.append("可用工具：\n");
        for (AgentTool tool : toolManager.getAllTools()) {
            context.append(String.format("- %s: %s\n", tool.getName(), tool.getDescription()));
        }
        
        // 添加上一次工具调用结果（如果有）
        if (lastToolResult != null) {
            context.append(String.format("\n上次工具调用结果：%s\n", lastToolResult.getFormattedResult()));
        }
        
        return context.toString();
    }
    
    /**
     * 生成行动决策
     * @return 行动决策字符串
     */
    private String generateActionDecision() {
        try {
            String actionPrompt = String.format("""
                基于前面的思考，请决定下一步行动：

                **重要规则：**
                1.  如果上一步是工具调用并且成功获取到信息，**必须**直接回复用户，**禁止**再次调用任何工具。
                2.  只有在确实需要新信息时，才调用工具。

                如果需要调用工具，请使用以下格式：
                工具名称(参数)
                
                例如：
                - orderQuery(12345)
                - dishRecommend(川菜)
                - storeStatus()
                
                如果不需要调用工具，请直接用自然语言回复用户。
                
                当前可用工具：
                %s
                
                请给出你的行动决策：
                """, toolManager.getToolsDescription());
            
            // 创建临时消息列表
            var actionMessages = getMessageHistory();
            actionMessages.add(new org.springframework.ai.chat.messages.UserMessage(actionPrompt));
            
            Prompt prompt = new Prompt(actionMessages);
            ChatResponse response = getChatModel().call(prompt);
            
            if (response != null && response.getResult() != null) {
                return response.getResult().getOutput().getText();
            }
            
            return "";
            
        } catch (Exception e) {
            log.error("生成行动决策失败", e);
            return "";
        }
    }
    
    /**
     * 解析并执行工具调用
     * @param actionDecision 行动决策
     * @return 工具执行结果
     */
    private ToolResult parseAndExecuteToolCall(String actionDecision) {
        if (actionDecision == null || actionDecision.trim().isEmpty()) {
            return null;
        }
        
        // 使用正则表达式匹配工具调用
        Matcher matcher = TOOL_CALL_PATTERN.matcher(actionDecision);
        
        if (matcher.find()) {
            String toolName = matcher.group(1).trim();
            String parameters = matcher.group(2).trim();
            
            log.info("解析到工具调用：{}({})", toolName, parameters);
            
            // 执行工具调用
            return toolManager.callTool(toolName, parameters);
        } else {
            // 尝试简单的工具名匹配
            String[] lines = actionDecision.split("\n");
            for (String line : lines) {
                line = line.trim().toLowerCase();
                for (String toolName : toolManager.getAllToolNames()) {
                    if (line.contains(toolName.toLowerCase())) {
                        log.info("匹配到工具名称：{}", toolName);
                        return toolManager.callTool(toolName, "");
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * 生成最终回复
     * @param toolResult 工具执行结果
     * @return 最终回复
     */
    private String generateFinalResponse(ToolResult toolResult) {
        try {
            String responsePrompt = String.format("""
                基于工具调用结果，请生成一个友好、专业的回复给用户：
                
                工具调用结果：%s
                
                请注意：
                1. 回复要简洁明了，用户友好
                2. 如果工具调用成功，整合结果信息给出有用的回复
                3. 如果工具调用失败，向用户道歉并提供替代方案
                4. 保持苍穹外卖客服的专业形象
                
                请生成回复：
                """, toolResult.getFormattedResult());
            
            var responseMessages = getMessageHistory();
            responseMessages.add(new org.springframework.ai.chat.messages.UserMessage(responsePrompt));
            
            Prompt prompt = new Prompt(responseMessages);
            ChatResponse response = getChatModel().call(prompt);
            
            if (response != null && response.getResult() != null) {
                String finalResponse = response.getResult().getOutput().getText();
                addAssistantMessage(finalResponse);
                return finalResponse;
            }
            
            // 备用回复
            if (toolResult.isSuccess()) {
                return String.format("根据查询结果：%s", toolResult.getData());
            } else {
                return String.format("抱歉，%s。请稍后再试或联系人工客服。", toolResult.getError());
            }
            
        } catch (Exception e) {
            log.error("生成最终回复失败", e);
            return AgentConstants.ERROR_GENERAL;
        }
    }
    
    /**
     * 添加工具到工具管理器
     * @param tool 工具实例
     * @return true表示添加成功
     */
    public boolean addTool(AgentTool tool) {
        return toolManager.registerTool(tool);
    }
    
    /**
     * 移除工具
     * @param toolName 工具名称
     * @return true表示移除成功
     */
    public boolean removeTool(String toolName) {
        return toolManager.unregisterTool(toolName);
    }
    
    /**
     * 获取工具调用统计
     * @return 调用统计信息
     */
    public String getToolCallStats() {
        return String.format("总工具调用次数：%d，工具详细统计：%s", 
                toolCallCount, toolManager.getCallStats());
    }
    
    /**
     * 获取可用工具列表
     * @return 工具列表描述
     */
    public String getAvailableTools() {
        return toolManager.getToolsDescription();
    }
}
