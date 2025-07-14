package com.sky.agent.core;

import com.sky.agent.constant.AgentConstants;
import com.sky.agent.constant.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * AI智能体基础抽象类
 * 提供智能体的基础功能和生命周期管理
 * 
 * 核心特性：
 * 1. 状态管理和生命周期控制
 * 2. 基于步骤的执行循环框架
 * 3. 支持同步和流式执行模式
 * 4. 内存管理和资源清理
 * 5. 异常处理和超时控制
 * 
 * @author wuzy
 * @since 2025-07-14
 */
@Data
@Slf4j
public abstract class BaseAgent {
    
    // ============== 核心属性 ==============
    
    /**
     * 智能体唯一标识
     */
    private String agentId;
    
    /**
     * 智能体名称
     */
    private String name;
    
    /**
     * 系统提示词
     */
    private String systemPrompt;
    
    /**
     * 下一步提示词
     */
    private String nextStepPrompt;
    
    // ============== 状态管理 ==============
    
    /**
     * 当前状态
     */
    private AgentState state = AgentState.IDLE;
    
    /**
     * 当前执行步骤
     */
    private int currentStep = 0;
    
    /**
     * 最大执行步骤数
     */
    private int maxSteps = AgentConstants.DEFAULT_MAX_STEPS;
    
    /**
     * 执行开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 超时时间（毫秒）
     */
    private long timeoutMs = AgentConstants.DEFAULT_TIMEOUT_MS;
    
    // ============== AI模型和记忆 ==============
    
    /**
     * AI聊天客户端
     */
    protected ChatClient chatClient;
    
    /**
     * 底层聊天模型
     */
    protected ChatModel chatModel;
    
    /**
     * 消息记忆列表
     */
    private List<Message> messageList = new ArrayList<>();
    
    /**
     * 当前会话ID
     */
    private String conversationId;
    
    // ============== 构造函数 ==============
    
    /**
     * 受保护的构造函数
     * @param name 智能体名称
     * @param systemPrompt 系统提示词
     * @param chatModel 聊天模型
     */
    protected BaseAgent(String name, String systemPrompt, ChatModel chatModel) {
        this.agentId = UUID.randomUUID().toString();
        this.name = name;
        this.systemPrompt = systemPrompt;
        this.nextStepPrompt = AgentConstants.REACT_NEXT_STEP_PROMPT;
        this.chatModel = chatModel;
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .build();
        
        log.info("智能体 [{}] 初始化完成，ID: {}", name, agentId);
    }
    
    // ============== 核心抽象方法 ==============
    
    /**
     * 执行单个步骤
     * 子类必须实现此方法定义具体的执行逻辑
     * 
     * @return 步骤执行结果，如果返回null表示需要继续执行下一步
     */
    protected abstract String step();
    
    /**
     * 初始化智能体
     * 子类可以重写此方法进行自定义初始化
     */
    protected void initialize() {
        // 默认空实现，子类可重写
        log.debug("智能体 [{}] 初始化", name);
    }
    
    /**
     * 清理资源
     * 子类可以重写此方法进行自定义清理
     */
    protected void cleanup() {
        // 默认清理操作
        this.messageList.clear();
        this.currentStep = 0;
        log.debug("智能体 [{}] 资源清理完成", name);
    }
    
    // ============== 核心执行方法 ==============
    
    /**
     * 运行智能体（同步模式）
     * 
     * @param input 用户输入
     * @return 执行结果
     */
    public String run(String input) {
        return run(input, null);
    }
    
    /**
     * 运行智能体（同步模式）
     * 
     * @param input 用户输入
     * @param conversationId 会话ID
     * @return 执行结果
     */
    public String run(String input, String conversationId) {
        try {
            // 1. 状态检查和初始化
            if (!validateState()) {
                return AgentConstants.ERROR_GENERAL;
            }
            
            this.conversationId = conversationId;
            this.state = AgentState.RUNNING;
            this.startTime = LocalDateTime.now();
            
            initialize();
            
            log.info("智能体 [{}] 开始执行任务，输入: {}", name, input);
            
            // 2. 添加用户输入到消息列表
            addUserMessage(input);
            
            // 3. 执行步骤循环
            String result = null;
            this.currentStep = 0;
            
            while (currentStep < maxSteps && !state.isTerminal()) {
                // 检查超时
                if (isTimeout()) {
                    this.state = AgentState.TIMEOUT;
                    log.warn("智能体 [{}] 执行超时", name);
                    return AgentConstants.ERROR_TIMEOUT;
                }
                
                currentStep++;
                log.debug("智能体 [{}] 执行第 {} 步", name, currentStep);
                
                try {
                    result = step();
                    if (result != null) {
                        // 有明确结果，任务完成
                        this.state = AgentState.COMPLETED;
                        break;
                    }
                } catch (Exception e) {
                    log.error("智能体 [{}] 第 {} 步执行失败", name, currentStep, e);
                    this.state = AgentState.ERROR;
                    return AgentConstants.ERROR_GENERAL;
                }
            }
            
            // 4. 检查是否超过最大步骤数
            if (currentStep >= maxSteps && !state.isTerminal()) {
                this.state = AgentState.ERROR;
                log.warn("智能体 [{}] 达到最大步骤数 {}", name, maxSteps);
                return AgentConstants.ERROR_MAX_STEPS_EXCEEDED;
            }
            
            // 5. 返回结果
            log.info("智能体 [{}] 任务完成，共执行 {} 步", name, currentStep);
            return result != null ? result : AgentConstants.SUCCESS_TASK_COMPLETED;
            
        } catch (Exception e) {
            log.error("智能体 [{}] 执行过程中发生异常", name, e);
            this.state = AgentState.ERROR;
            return AgentConstants.ERROR_GENERAL;
        } finally {
            // 清理资源
            cleanup();
            this.state = AgentState.IDLE;
        }
    }
    
    /**
     * 运行智能体（流式模式）
     * 
     * @param input 用户输入
     * @return SSE发射器
     */
    public SseEmitter runStream(String input) {
        return runStream(input, null);
    }
    
    /**
     * 运行智能体（流式模式）
     * 
     * @param input 用户输入
     * @param conversationId 会话ID
     * @return SSE发射器
     */
    public SseEmitter runStream(String input, String conversationId) {
        SseEmitter sseEmitter = new SseEmitter(AgentConstants.DEFAULT_SSE_TIMEOUT_MS);
        
        // 异步执行
        CompletableFuture.runAsync(() -> {
            try {
                String result = run(input, conversationId);
                sseEmitter.send(result);
                sseEmitter.complete();
            } catch (Exception e) {
                log.error("流式执行失败", e);
                try {
                    sseEmitter.send(AgentConstants.ERROR_GENERAL);
                } catch (Exception sendError) {
                    log.error("发送错误消息失败", sendError);
                }
                sseEmitter.completeWithError(e);
            }
        });
        
        return sseEmitter;
    }
    
    // ============== 辅助方法 ==============
    
    /**
     * 验证智能体状态
     * @return true表示状态有效
     */
    private boolean validateState() {
        if (chatClient == null || chatModel == null) {
            log.error("智能体 [{}] 未正确初始化", name);
            return false;
        }
        
        if (state == AgentState.RUNNING) {
            log.warn("智能体 [{}] 正在运行中，请稍后再试", name);
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查是否超时
     * @return true表示已超时
     */
    private boolean isTimeout() {
        if (startTime == null) return false;
        
        long elapsedMs = java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();
        return elapsedMs > timeoutMs;
    }
    
    /**
     * 添加用户消息
     * @param content 消息内容
     */
    protected void addUserMessage(String content) {
        messageList.add(new org.springframework.ai.chat.messages.UserMessage(content));
        log.debug("添加用户消息: {}", content);
    }
    
    /**
     * 添加助手消息
     * @param content 消息内容
     */
    protected void addAssistantMessage(String content) {
        messageList.add(new org.springframework.ai.chat.messages.AssistantMessage(content));
        log.debug("添加助手消息: {}", content);
    }
    
    /**
     * 获取消息历史的副本
     * @return 消息列表副本
     */
    protected List<Message> getMessageHistory() {
        return new ArrayList<>(messageList);
    }
    
    /**
     * 重置智能体状态
     */
    public void reset() {
        this.state = AgentState.IDLE;
        this.currentStep = 0;
        this.startTime = null;
        this.messageList.clear();
        this.conversationId = null;
        log.info("智能体 [{}] 状态已重置", name);
    }
    
    /**
     * 获取智能体状态信息
     * @return 状态信息字符串
     */
    public String getStatusInfo() {
        return String.format("智能体[%s] - 状态: %s, 步骤: %d/%d, ID: %s", 
                name, state.getDescription(), currentStep, maxSteps, agentId);
    }
}
