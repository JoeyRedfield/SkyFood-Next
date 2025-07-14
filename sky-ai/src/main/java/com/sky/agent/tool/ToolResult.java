package com.sky.agent.tool;

import lombok.Data;

/**
 * 工具调用结果封装类
 * 
 * @author wuzy
 * @since 2025-07-14
 */
@Data
public class ToolResult {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 结果数据
     */
    private String data;
    
    /**
     * 错误信息
     */
    private String error;
    
    /**
     * 工具名称
     */
    private String toolName;
    
    /**
     * 执行时间（毫秒）
     */
    private long executionTime;
    
    /**
     * 私有构造函数
     */
    private ToolResult() {}
    
    /**
     * 创建成功结果
     * @param toolName 工具名称
     * @param data 结果数据
     * @return 成功结果
     */
    public static ToolResult success(String toolName, String data) {
        ToolResult result = new ToolResult();
        result.setSuccess(true);
        result.setToolName(toolName);
        result.setData(data);
        return result;
    }
    
    /**
     * 创建成功结果（带执行时间）
     * @param toolName 工具名称
     * @param data 结果数据
     * @param executionTime 执行时间
     * @return 成功结果
     */
    public static ToolResult success(String toolName, String data, long executionTime) {
        ToolResult result = success(toolName, data);
        result.setExecutionTime(executionTime);
        return result;
    }
    
    /**
     * 创建失败结果
     * @param toolName 工具名称
     * @param error 错误信息
     * @return 失败结果
     */
    public static ToolResult failure(String toolName, String error) {
        ToolResult result = new ToolResult();
        result.setSuccess(false);
        result.setToolName(toolName);
        result.setError(error);
        return result;
    }
    
    /**
     * 创建失败结果（带执行时间）
     * @param toolName 工具名称
     * @param error 错误信息
     * @param executionTime 执行时间
     * @return 失败结果
     */
    public static ToolResult failure(String toolName, String error, long executionTime) {
        ToolResult result = failure(toolName, error);
        result.setExecutionTime(executionTime);
        return result;
    }
    
    /**
     * 获取格式化的结果字符串
     * @return 格式化结果
     */
    public String getFormattedResult() {
        if (success) {
            return String.format("[%s] 执行成功: %s", toolName, data);
        } else {
            return String.format("[%s] 执行失败: %s", toolName, error);
        }
    }
    
    /**
     * 判断是否有数据
     * @return true表示有数据
     */
    public boolean hasData() {
        return data != null && !data.trim().isEmpty();
    }
    
    /**
     * 判断是否有错误
     * @return true表示有错误
     */
    public boolean hasError() {
        return error != null && !error.trim().isEmpty();
    }
}
