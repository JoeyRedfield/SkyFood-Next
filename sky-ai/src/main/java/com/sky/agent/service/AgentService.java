package com.sky.agent.service;

import com.sky.agent.app.CustomerServiceAgent;
import com.sky.agent.tool.ToolManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI智能体服务
 * 为外部模块提供智能体功能的统一接口
 * 
 * @author wuzy
 * @since 2025-07-14
 */
@Service
@Slf4j
public class AgentService {
    
    @Autowired
    private CustomerServiceAgent customerServiceAgent;
    
    /**
     * 处理用户消息（同步）
     * @param message 用户消息
     * @param userId 用户ID
     * @return AI回复
     */
    public String handleMessage(String message, String userId) {
        try {
            log.info("AgentService处理用户[{}]消息：{}", userId, message);
            
            // 首先尝试快速回复
            String quickReply = customerServiceAgent.getQuickReply(message);
            if (quickReply != null) {
                log.info("使用快速回复：{}", quickReply);
                return quickReply;
            }
            
            // 使用智能体处理
            String response = customerServiceAgent.handleCustomerInquiry(message, userId);
            log.info("智能体回复：{}", response);
            
            return response;
            
        } catch (Exception e) {
            log.error("处理用户消息失败", e);
            return "抱歉，我现在遇到了一些技术问题，请稍后再试或联系人工客服：400-8888-888";
        }
    }
    
    /**
     * 处理用户消息（流式）
     * @param message 用户消息
     * @param userId 用户ID
     * @return SSE发射器
     */
    public SseEmitter handleMessageStream(String message, String userId) {
        try {
            log.info("AgentService流式处理用户[{}]消息：{}", userId, message);
            
            // 检查是否可以快速回复
            String quickReply = customerServiceAgent.getQuickReply(message);
            if (quickReply != null) {
                // 快速回复也通过SSE返回
                SseEmitter emitter = new SseEmitter(30000L);
                try {
                    emitter.send(quickReply);
                    emitter.complete();
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
                return emitter;
            }
            
            // 使用智能体流式处理
            return customerServiceAgent.runStream(message, userId);
            
        } catch (Exception e) {
            log.error("流式处理用户消息失败", e);
            SseEmitter emitter = new SseEmitter(30000L);
            try {
                emitter.send("抱歉，我现在遇到了一些技术问题，请稍后再试或联系人工客服：400-8888-888");
                emitter.complete();
            } catch (Exception sendError) {
                emitter.completeWithError(sendError);
            }
            return emitter;
        }
    }
    
    /**
     * 重置智能体状态
     * @param userId 用户ID
     */
    public void resetAgent(String userId) {
        try {
            customerServiceAgent.reset();
            log.info("用户[{}]的智能体状态已重置", userId);
        } catch (Exception e) {
            log.error("重置智能体状态失败", e);
        }
    }
    
    /**
     * 获取智能体状态信息
     * @return 状态信息
     */
    public String getAgentStatus() {
        try {
            return customerServiceAgent.getStatusReport();
        } catch (Exception e) {
            log.error("获取智能体状态失败", e);
            return "无法获取智能体状态信息";
        }
    }
    
    /**
     * 获取工具使用统计
     * @return 工具统计信息
     */
    public String getToolStats() {
        try {
            ToolManager toolManager = ToolManager.getInstance();
            return String.format("工具统计：共%d个工具，调用统计：%s", 
                    toolManager.getToolCount(), 
                    toolManager.getCallStats());
        } catch (Exception e) {
            log.error("获取工具统计失败", e);
            return "无法获取工具统计信息";
        }
    }
    
    /**
     * 检查智能体是否可用
     * @return true表示可用
     */
    public boolean isAgentAvailable() {
        try {
            return customerServiceAgent != null && 
                   customerServiceAgent.getState() != null;
        } catch (Exception e) {
            log.error("检查智能体可用性失败", e);
            return false;
        }
    }
}
