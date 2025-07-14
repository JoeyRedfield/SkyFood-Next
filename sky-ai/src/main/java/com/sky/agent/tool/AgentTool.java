package com.sky.agent.tool;

/**
 * 智能体工具接口
 * 定义工具的基本行为规范
 * 
 * @author wuzy
 * @since 2025-07-14
 */
public interface AgentTool {
    
    /**
     * 获取工具名称
     * @return 工具名称
     */
    String getName();
    
    /**
     * 获取工具描述
     * @return 工具描述
     */
    String getDescription();
    
    /**
     * 获取工具参数说明
     * @return 参数说明
     */
    String getParameterDescription();
    
    /**
     * 执行工具
     * @param parameters 工具参数
     * @return 执行结果
     */
    ToolResult execute(String parameters);
    
    /**
     * 验证参数
     * @param parameters 参数
     * @return true表示参数有效
     */
    default boolean validateParameters(String parameters) {
        return parameters != null && !parameters.trim().isEmpty();
    }
    
    /**
     * 获取工具类型
     * @return 工具类型
     */
    default String getType() {
        return "general";
    }
    
    /**
     * 是否异步执行
     * @return true表示异步执行
     */
    default boolean isAsync() {
        return false;
    }
    
    /**
     * 获取工具的完整描述（包括参数）
     * @return 完整描述
     */
    default String getFullDescription() {
        return String.format("%s - %s\n参数说明: %s", 
                getName(), getDescription(), getParameterDescription());
    }
}
