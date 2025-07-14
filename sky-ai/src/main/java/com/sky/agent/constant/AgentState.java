package com.sky.agent.constant;

/**
 * AI智能体状态枚举
 * 用于管理智能体的生命周期状态
 * 
 * @author wuzy
 * @since 2025-07-14
 */
public enum AgentState {
    
    /**
     * 空闲状态 - 智能体初始化完成，等待任务
     */
    IDLE("idle", "空闲状态"),
    
    /**
     * 运行状态 - 智能体正在执行任务
     */
    RUNNING("running", "运行状态"),
    
    /**
     * 思考状态 - 智能体正在分析和推理
     */
    THINKING("thinking", "思考状态"),
    
    /**
     * 执行状态 - 智能体正在执行工具调用
     */
    ACTING("acting", "执行状态"),
    
    /**
     * 等待状态 - 智能体等待外部输入或响应
     */
    WAITING("waiting", "等待状态"),
    
    /**
     * 完成状态 - 智能体已完成当前任务
     */
    COMPLETED("completed", "完成状态"),
    
    /**
     * 错误状态 - 智能体执行过程中出现错误
     */
    ERROR("error", "错误状态"),
    
    /**
     * 超时状态 - 智能体执行超时
     */
    TIMEOUT("timeout", "超时状态"),
    
    /**
     * 停止状态 - 智能体被手动停止
     */
    STOPPED("stopped", "停止状态");
    
    private final String code;
    private final String description;
    
    AgentState(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据状态码获取枚举
     * @param code 状态码
     * @return 对应的状态枚举
     */
    public static AgentState fromCode(String code) {
        for (AgentState state : values()) {
            if (state.getCode().equals(code)) {
                return state;
            }
        }
        throw new IllegalArgumentException("未知的智能体状态码: " + code);
    }
    
    /**
     * 判断是否为终止状态
     * @return true表示终止状态
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == ERROR || this == TIMEOUT || this == STOPPED;
    }
    
    /**
     * 判断是否为活跃状态
     * @return true表示活跃状态
     */
    public boolean isActive() {
        return this == RUNNING || this == THINKING || this == ACTING || this == WAITING;
    }
}
