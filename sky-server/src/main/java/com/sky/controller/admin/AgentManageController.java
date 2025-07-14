package com.sky.controller.admin;

import com.sky.agent.service.AgentService;
import com.sky.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员AI智能体管理控制器
 * 提供智能体系统管理功能
 * 
 * @author wuzy
 * @since 2025-07-14
 */
@RestController
@RequestMapping("/admin/agent")
@Tag(name = "AI智能体管理", description = "管理员管理AI智能体系统")
@Slf4j
public class AgentManageController {
    
    @Autowired
    private AgentService agentService;
    
    /**
     * 获取智能体状态报告
     * @return 详细状态信息
     */
    @GetMapping("/status")
    @Operation(summary = "获取智能体状态报告", description = "获取智能体详细运行状态信息")
    public Result<String> getAgentStatus() {
        try {
            String status = agentService.getAgentStatus();
            return Result.success(status);
        } catch (Exception e) {
            log.error("获取智能体状态失败", e);
            return Result.error("获取状态失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取工具调用统计
     * @return 工具使用统计信息
     */
    @GetMapping("/tool-stats")
    @Operation(summary = "获取工具调用统计", description = "获取所有工具的调用次数和性能统计")
    public Result<String> getToolStats() {
        try {
            String stats = agentService.getToolStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取工具统计失败", e);
            return Result.error("获取统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 重置智能体系统
     * @return 操作结果
     */
    @PostMapping("/reset")
    @Operation(summary = "重置智能体系统", description = "重置整个智能体系统状态和统计信息")
    public Result<String> resetAgent() {
        try {
            agentService.resetAgent("admin");
            log.info("管理员重置智能体系统");
            return Result.success("智能体系统已重置");
        } catch (Exception e) {
            log.error("重置智能体系统失败", e);
            return Result.error("重置失败：" + e.getMessage());
        }
    }
    
    /**
     * 健康检查
     * @return 健康状态
     */
    @GetMapping("/health")
    @Operation(summary = "智能体系统健康检查", description = "检查智能体系统是否正常运行")
    public Result<Boolean> healthCheck() {
        try {
            boolean healthy = agentService.isAgentAvailable();
            log.info("智能体系统健康检查结果：{}", healthy);
            return Result.success(healthy);
        } catch (Exception e) {
            log.error("智能体系统健康检查失败", e);
            return Result.success(false);
        }
    }
    
    /**
     * 测试智能体功能
     * @param testMessage 测试消息
     * @return 测试结果
     */
    @PostMapping("/test")
    @Operation(summary = "测试智能体功能", description = "使用测试消息验证智能体功能")
    public Result<String> testAgent(@RequestParam String testMessage) {
        try {
            log.info("管理员测试智能体功能，消息：{}", testMessage);
            String response = agentService.handleMessage(testMessage, "admin-test");
            log.info("智能体测试回复：{}", response);
            return Result.success(response);
        } catch (Exception e) {
            log.error("智能体功能测试失败", e);
            return Result.error("测试失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取系统信息
     * @return 系统信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取智能体系统信息", description = "获取智能体系统配置和版本信息")
    public Result<String> getSystemInfo() {
        try {
            StringBuilder info = new StringBuilder();
            info.append("=== AI智能体系统信息 ===\n");
            info.append("系统版本：1.0.0\n");
            info.append("框架：ReAct智能体架构\n");
            info.append("工具数量：").append(getToolCount()).append("\n");
            info.append("可用状态：").append(agentService.isAgentAvailable() ? "正常" : "异常").append("\n");
            info.append("=========================");
            
            return Result.success(info.toString());
        } catch (Exception e) {
            log.error("获取系统信息失败", e);
            return Result.error("获取信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取工具数量
     * @return 工具数量
     */
    private int getToolCount() {
        try {
            String stats = agentService.getToolStats();
            // 从统计信息中解析工具数量
            if (stats.contains("共") && stats.contains("个工具")) {
                int start = stats.indexOf("共") + 1;
                int end = stats.indexOf("个工具");
                String countStr = stats.substring(start, end);
                return Integer.parseInt(countStr);
            }
            return 4; // 默认工具数量
        } catch (Exception e) {
            return 4; // 出错时返回默认值
        }
    }
}
