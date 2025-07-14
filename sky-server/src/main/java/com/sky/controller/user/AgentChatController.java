package com.sky.controller.user;

import com.sky.agent.service.AgentService;
import com.sky.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;

/**
 * AI智能体聊天控制器
 * 提供智能客服聊天功能
 * 
 * @author wuzy
 * @since 2025-07-14
 */
@RestController
@RequestMapping("/user/chat")
@Tag(name = "AI智能体聊天", description = "提供智能客服聊天功能")
@Slf4j
public class AgentChatController {
    
    @Autowired
    private AgentService agentService;
    
    /**
     * 发送消息给AI智能体（同步）
     * @param message 用户消息
     * @param request HTTP请求
     * @return AI回复
     */
    @PostMapping("/message")
    @Operation(summary = "发送消息给AI智能体", description = "同步处理用户消息并返回AI回复")
    public Result<String> sendMessage(@RequestParam String message, 
                                     HttpServletRequest request) {
        try {
            // 从JWT或session中获取用户ID，这里先用IP作为标识
            String userId = getUserId(request);
            
            // 调用智能体服务
            String response = agentService.handleMessage(message, userId);
            
            log.info("用户[{}]消息处理完成", userId);
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("处理用户消息失败", e);
            return Result.error("消息处理失败，请稍后重试");
        }
    }
    
    /**
     * 发送消息给AI智能体（流式）
     * @param message 用户消息
     * @param request HTTP请求
     * @return SSE流
     */
    @GetMapping(value = "/message/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式发送消息给AI智能体", description = "流式处理用户消息，实时返回AI回复")
    public SseEmitter sendMessageStream(@RequestParam String message, 
                                       HttpServletRequest request) {
        try {
            // 从JWT或session中获取用户ID，这里先用IP作为标识
            String userId = getUserId(request);
            
            log.info("开始流式处理用户[{}]消息：{}", userId, message);
            
            // 调用智能体服务
            return agentService.handleMessageStream(message, userId);
            
        } catch (Exception e) {
            log.error("流式处理用户消息失败", e);
            SseEmitter emitter = new SseEmitter(30000L);
            try {
                emitter.send("消息处理失败，请稍后重试");
                emitter.complete();
            } catch (Exception sendError) {
                emitter.completeWithError(sendError);
            }
            return emitter;
        }
    }
    
    /**
     * 重置聊天会话
     * @param request HTTP请求
     * @return 操作结果
     */
    @PostMapping("/reset")
    @Operation(summary = "重置聊天会话", description = "清除当前用户的聊天历史和智能体状态")
    public Result<String> resetChat(HttpServletRequest request) {
        try {
            String userId = getUserId(request);
            agentService.resetAgent(userId);
            
            log.info("用户[{}]聊天会话已重置", userId);
            
            return Result.success("聊天会话已重置");
            
        } catch (Exception e) {
            log.error("重置聊天会话失败", e);
            return Result.error("重置失败，请稍后重试");
        }
    }
    
    /**
     * 获取智能体状态
     * @return 状态信息
     */
    @GetMapping("/status")
    @Operation(summary = "获取智能体状态", description = "获取智能体当前运行状态信息")
    public Result<String> getStatus() {
        try {
            String status = agentService.getAgentStatus();
            return Result.success(status);
        } catch (Exception e) {
            log.error("获取智能体状态失败", e);
            return Result.error("获取状态失败");
        }
    }
    
    /**
     * 获取工具使用统计
     * @return 统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取工具使用统计", description = "获取智能体工具调用次数和性能统计")
    public Result<String> getStats() {
        try {
            String stats = agentService.getToolStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取工具统计失败", e);
            return Result.error("获取统计失败");
        }
    }
    
    /**
     * 检查智能体健康状态
     * @return 健康状态
     */
    @GetMapping("/health")
    @Operation(summary = "检查智能体健康状态", description = "检查智能体是否正常工作")
    public Result<Boolean> checkHealth() {
        try {
            boolean available = agentService.isAgentAvailable();
            return Result.success(available);
        } catch (Exception e) {
            log.error("检查智能体健康状态失败", e);
            return Result.error("健康检查失败");
        }
    }
    
    /**
     * 获取用户ID
     * @param request HTTP请求
     * @return 用户ID
     */
    private String getUserId(HttpServletRequest request) {
        // TODO: 实际项目中应该从JWT token或session中获取用户ID
        // 这里临时使用IP地址作为用户标识
        String userId = request.getHeader("X-Forwarded-For");
        if (userId == null || userId.isEmpty()) {
            userId = request.getRemoteAddr();
        }
        
        // 如果还是为空，使用默认值
        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }
        
        return userId;
    }
}
