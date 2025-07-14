package com.sky.agent.tool;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具管理器
 * 负责工具的注册、管理和调用
 * 
 * @author wuzy
 * @since 2025-07-14
 */
@Slf4j
public class ToolManager {
    
    /**
     * 工具注册表 - 线程安全
     */
    private final Map<String, AgentTool> tools = new ConcurrentHashMap<>();
    
    /**
     * 工具调用统计
     */
    private final Map<String, Integer> callStats = new ConcurrentHashMap<>();
    
    /**
     * 单例实例
     */
    private static volatile ToolManager instance;
    
    /**
     * 私有构造函数
     */
    private ToolManager() {}
    
    /**
     * 获取单例实例
     * @return 工具管理器实例
     */
    public static ToolManager getInstance() {
        if (instance == null) {
            synchronized (ToolManager.class) {
                if (instance == null) {
                    instance = new ToolManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 注册工具
     * @param tool 工具实例
     * @return true表示注册成功
     */
    public boolean registerTool(AgentTool tool) {
        if (tool == null || tool.getName() == null || tool.getName().trim().isEmpty()) {
            log.warn("无法注册无效的工具");
            return false;
        }
        
        String toolName = tool.getName().toLowerCase().trim();
        
        if (tools.containsKey(toolName)) {
            log.warn("工具 [{}] 已存在，将被覆盖", toolName);
        }
        
        tools.put(toolName, tool);
        callStats.put(toolName, 0);
        log.info("工具 [{}] 注册成功: {}", toolName, tool.getDescription());
        
        return true;
    }
    
    /**
     * 批量注册工具
     * @param toolList 工具列表
     * @return 成功注册的工具数量
     */
    public int registerTools(List<AgentTool> toolList) {
        if (toolList == null || toolList.isEmpty()) {
            return 0;
        }
        
        int successCount = 0;
        for (AgentTool tool : toolList) {
            if (registerTool(tool)) {
                successCount++;
            }
        }
        
        log.info("批量注册工具完成，成功注册 {} 个工具", successCount);
        return successCount;
    }
    
    /**
     * 注销工具
     * @param toolName 工具名称
     * @return true表示注销成功
     */
    public boolean unregisterTool(String toolName) {
        if (toolName == null || toolName.trim().isEmpty()) {
            return false;
        }
        
        String normalizedName = toolName.toLowerCase().trim();
        AgentTool removed = tools.remove(normalizedName);
        callStats.remove(normalizedName);
        
        if (removed != null) {
            log.info("工具 [{}] 注销成功", normalizedName);
            return true;
        } else {
            log.warn("尝试注销不存在的工具 [{}]", normalizedName);
            return false;
        }
    }
    
    /**
     * 获取工具
     * @param toolName 工具名称
     * @return 工具实例，如果不存在返回null
     */
    public AgentTool getTool(String toolName) {
        if (toolName == null || toolName.trim().isEmpty()) {
            return null;
        }
        
        return tools.get(toolName.toLowerCase().trim());
    }
    
    /**
     * 检查工具是否存在
     * @param toolName 工具名称
     * @return true表示工具存在
     */
    public boolean hasTool(String toolName) {
        return getTool(toolName) != null;
    }
    
    /**
     * 调用工具
     * @param toolName 工具名称
     * @param parameters 工具参数
     * @return 工具执行结果
     */
    public ToolResult callTool(String toolName, String parameters) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 查找工具
            AgentTool tool = getTool(toolName);
            if (tool == null) {
                log.warn("调用不存在的工具: {}", toolName);
                return ToolResult.failure(toolName, "工具不存在");
            }
            
            // 验证参数
            if (!tool.validateParameters(parameters)) {
                log.warn("工具 [{}] 参数验证失败: {}", toolName, parameters);
                return ToolResult.failure(toolName, "参数验证失败");
            }
            
            log.info("调用工具 [{}]，参数: {}", toolName, parameters);
            
            // 执行工具
            ToolResult result = tool.execute(parameters);
            
            // 更新调用统计
            String normalizedName = toolName.toLowerCase().trim();
            callStats.merge(normalizedName, 1, Integer::sum);
            
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            result.setExecutionTime(executionTime);
            
            if (result.isSuccess()) {
                log.info("工具 [{}] 执行成功，耗时: {}ms", toolName, executionTime);
            } else {
                log.warn("工具 [{}] 执行失败: {}，耗时: {}ms", toolName, result.getError(), executionTime);
            }
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("工具 [{}] 执行异常，耗时: {}ms", toolName, executionTime, e);
            return ToolResult.failure(toolName, "工具执行异常: " + e.getMessage(), executionTime);
        }
    }
    
    /**
     * 获取所有已注册的工具列表
     * @return 工具列表
     */
    public List<AgentTool> getAllTools() {
        return new ArrayList<>(tools.values());
    }
    
    /**
     * 获取所有工具名称
     * @return 工具名称列表
     */
    public List<String> getAllToolNames() {
        return new ArrayList<>(tools.keySet());
    }
    
    /**
     * 获取工具数量
     * @return 工具数量
     */
    public int getToolCount() {
        return tools.size();
    }
    
    /**
     * 获取工具调用统计
     * @return 调用统计Map
     */
    public Map<String, Integer> getCallStats() {
        return new HashMap<>(callStats);
    }
    
    /**
     * 重置调用统计
     */
    public void resetCallStats() {
        callStats.clear();
        log.info("工具调用统计已重置");
    }
    
    /**
     * 获取工具列表描述
     * @return 格式化的工具列表
     */
    public String getToolsDescription() {
        if (tools.isEmpty()) {
            return "暂无可用工具";
        }
        
        StringBuilder description = new StringBuilder();
        description.append("可用工具列表：\n");
        
        for (AgentTool tool : tools.values()) {
            description.append(String.format("- %s: %s\n", 
                    tool.getName(), tool.getDescription()));
        }
        
        return description.toString();
    }
    
    /**
     * 清空所有工具
     */
    public void clear() {
        int count = tools.size();
        tools.clear();
        callStats.clear();
        log.info("已清空所有工具，共清空 {} 个工具", count);
    }
    
    /**
     * 根据类型获取工具
     * @param type 工具类型
     * @return 指定类型的工具列表
     */
    public List<AgentTool> getToolsByType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return tools.values().stream()
                .filter(tool -> type.equalsIgnoreCase(tool.getType()))
                .toList();
    }
}
