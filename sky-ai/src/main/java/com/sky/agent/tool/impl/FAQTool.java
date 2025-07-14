package com.sky.agent.tool.impl;

import com.sky.agent.tool.AgentTool;
import com.sky.agent.tool.ToolResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 常见问题解答工具
 * 自动回答用户的常见问题，提供快速的问题解决方案
 * 
 * 功能：
 * 1. 基于关键词匹配回答常见问题
 * 2. 提供分类问题浏览
 * 3. 搜索相关问题
 * 4. 智能推荐相关问题
 * 5. 统计问题访问频率
 * 
 * @author wuzy
 * @since 2025-07-14
 */
@Slf4j
public class FAQTool implements AgentTool {
    
    /**
     * FAQ服务接口
     */
    private final FAQService faqService;
    
    /**
     * 问题访问统计
     */
    private final Map<String, Integer> questionStats = new HashMap<>();
    
    /**
     * 构造函数
     * @param faqService FAQ服务
     */
    public FAQTool(FAQService faqService) {
        this.faqService = faqService;
    }
    
    @Override
    public String getName() {
        return "faq";
    }
    
    @Override
    public String getDescription() {
        return "查询和解答常见问题";
    }
    
    @Override
    public String getParameterDescription() {
        return "问题关键词或问题内容（必须）- 可以是具体问题、关键词或问题分类，如：'退款'、'配送时间'、'支付问题'";
    }
    
    @Override
    public String getType() {
        return "faq";
    }
    
    @Override
    public boolean validateParameters(String parameters) {
        return parameters != null && !parameters.trim().isEmpty();
    }
    
    @Override
    public ToolResult execute(String parameters) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始查询FAQ，关键词：{}", parameters);
            
            String keyword = parameters.trim();
            List<FAQItem> faqItems = faqService.searchFAQ(keyword);
            
            if (faqItems == null || faqItems.isEmpty()) {
                // 没找到匹配的FAQ，尝试模糊搜索
                faqItems = faqService.fuzzySearchFAQ(keyword);
            }
            
            String result;
            if (faqItems == null || faqItems.isEmpty()) {
                result = generateNoResultResponse(keyword);
            } else {
                result = formatFAQResults(faqItems, keyword);
                // 更新访问统计
                updateQuestionStats(faqItems);
            }
            
            log.info("FAQ查询完成，关键词：{}，找到{}条结果", keyword, 
                    faqItems != null ? faqItems.size() : 0);
            return ToolResult.success(getName(), result, System.currentTimeMillis() - startTime);
            
        } catch (Exception e) {
            log.error("FAQ查询失败，关键词：{}", parameters, e);
            return ToolResult.failure(getName(), 
                    "查询常见问题时发生错误，请稍后再试", 
                    System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * 格式化FAQ结果
     * @param faqItems FAQ条目列表
     * @param keyword 搜索关键词
     * @return 格式化后的结果
     */
    private String formatFAQResults(List<FAQItem> faqItems, String keyword) {
        StringBuilder result = new StringBuilder();
        
        result.append(String.format("🔍 关于\"%s\"的常见问题解答：\n\n", keyword));
        
        // 限制显示条数，避免信息过多
        int maxResults = Math.min(faqItems.size(), 5);
        
        for (int i = 0; i < maxResults; i++) {
            FAQItem item = faqItems.get(i);
            
            result.append(String.format("**Q%d: %s**\n", i + 1, item.getQuestion()));
            result.append(String.format("A%d: %s\n", i + 1, item.getAnswer()));
            
            // 添加相关操作提示
            if (item.getActions() != null && !item.getActions().isEmpty()) {
                result.append("🔧 相关操作：");
                for (String action : item.getActions()) {
                    result.append(action).append(" ");
                }
                result.append("\n");
            }
            
            result.append("\n");
        }
        
        // 如果有更多结果，提示用户
        if (faqItems.size() > maxResults) {
            result.append(String.format("📝 还有 %d 条相关问题，请尝试更具体的关键词搜索。\n\n", 
                    faqItems.size() - maxResults));
        }
        
        // 添加推荐的相关问题
        List<FAQItem> relatedQuestions = faqService.getRelatedQuestions(keyword);
        if (relatedQuestions != null && !relatedQuestions.isEmpty()) {
            result.append("💡 您可能还想了解：\n");
            for (int i = 0; i < Math.min(relatedQuestions.size(), 3); i++) {
                result.append(String.format("- %s\n", relatedQuestions.get(i).getQuestion()));
            }
            result.append("\n");
        }
        
        // 添加帮助提示
        result.append("❓ 如果以上回答没有解决您的问题，您可以：\n");
        result.append("- 尝试使用其他关键词重新搜索\n");
        result.append("- 联系人工客服：400-8888-888\n");
        result.append("- 在APP内提交意见反馈");
        
        return result.toString();
    }
    
    /**
     * 生成无结果响应
     * @param keyword 搜索关键词
     * @return 无结果响应
     */
    private String generateNoResultResponse(String keyword) {
        StringBuilder result = new StringBuilder();
        
        result.append(String.format("❌ 很抱歉，没有找到关于\"%s\"的相关问题。\n\n", keyword));
        
        // 提供常见问题分类
        result.append("📚 您可以浏览以下常见问题分类：\n");
        List<String> categories = faqService.getFAQCategories();
        if (categories != null && !categories.isEmpty()) {
            for (String category : categories) {
                result.append(String.format("- %s\n", category));
            }
        } else {
            result.append("- 订单问题\n");
            result.append("- 支付问题\n");
            result.append("- 配送问题\n");
            result.append("- 退款问题\n");
            result.append("- 账户问题\n");
        }
        
        result.append("\n🔍 搜索建议：\n");
        result.append("- 尝试使用更简单的关键词\n");
        result.append("- 检查拼写是否正确\n");
        result.append("- 尝试使用问题的核心词汇\n\n");
        
        result.append("🤝 需要人工帮助？\n");
        result.append("- 客服热线：400-8888-888\n");
        result.append("- 服务时间：9:00-21:00\n");
        result.append("- 或者直接告诉我您遇到的具体问题，我来为您解答！");
        
        return result.toString();
    }
    
    /**
     * 更新问题访问统计
     * @param faqItems FAQ条目列表
     */
    private void updateQuestionStats(List<FAQItem> faqItems) {
        for (FAQItem item : faqItems) {
            String questionId = item.getId().toString();
            questionStats.merge(questionId, 1, Integer::sum);
        }
    }
    
    /**
     * 获取热门问题统计
     * @return 问题访问统计
     */
    public Map<String, Integer> getQuestionStats() {
        return new HashMap<>(questionStats);
    }
    
    /**
     * FAQ服务接口
     */
    public interface FAQService {
        /**
         * 搜索FAQ
         * @param keyword 关键词
         * @return FAQ列表
         */
        List<FAQItem> searchFAQ(String keyword);
        
        /**
         * 模糊搜索FAQ
         * @param keyword 关键词
         * @return FAQ列表
         */
        List<FAQItem> fuzzySearchFAQ(String keyword);
        
        /**
         * 获取相关问题
         * @param keyword 关键词
         * @return 相关问题列表
         */
        List<FAQItem> getRelatedQuestions(String keyword);
        
        /**
         * 获取FAQ分类
         * @return 分类列表
         */
        List<String> getFAQCategories();
        
        /**
         * 根据分类获取FAQ
         * @param category 分类
         * @return FAQ列表
         */
        List<FAQItem> getFAQByCategory(String category);
    }
    
    /**
     * FAQ条目实体类
     */
    public static class FAQItem {
        private Long id;                        // FAQ ID
        private String question;                // 问题
        private String answer;                  // 答案
        private String category;                // 分类
        private List<String> keywords;          // 关键词
        private List<String> actions;           // 相关操作
        private Integer priority;               // 优先级
        private Integer viewCount;              // 查看次数
        private Boolean isActive;               // 是否启用
        private String createTime;              // 创建时间
        private String updateTime;              // 更新时间
        
        // 构造函数
        public FAQItem() {
            this.keywords = new ArrayList<>();
            this.actions = new ArrayList<>();
        }
        
        // getter和setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public List<String> getKeywords() { return keywords; }
        public void setKeywords(List<String> keywords) { this.keywords = keywords; }
        
        public List<String> getActions() { return actions; }
        public void setActions(List<String> actions) { this.actions = actions; }
        
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
        
        public Integer getViewCount() { return viewCount; }
        public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        
        public String getCreateTime() { return createTime; }
        public void setCreateTime(String createTime) { this.createTime = createTime; }
        
        public String getUpdateTime() { return updateTime; }
        public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    }
}
