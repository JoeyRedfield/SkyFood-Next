package com.sky.agent.tool.impl;

import com.sky.agent.tool.AgentTool;
import com.sky.agent.tool.ToolResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;

/**
 * 菜品推荐工具
 * 根据用户偏好和历史订单推荐合适的菜品
 * 
 * 功能：
 * 1. 根据菜系推荐热门菜品
 * 2. 基于价格区间推荐
 * 3. 根据用户历史订单推荐
 * 4. 推荐今日特价菜品
 * 5. 根据时间推荐（早餐、午餐、晚餐）
 * 
 * @author wuzy
 * @since 2025-07-14
 */
@Slf4j
public class DishRecommendTool implements AgentTool {
    
    /**
     * 菜品推荐服务接口
     */
    private final DishRecommendService dishRecommendService;
    
    /**
     * 构造函数
     * @param dishRecommendService 菜品推荐服务
     */
    public DishRecommendTool(DishRecommendService dishRecommendService) {
        this.dishRecommendService = dishRecommendService;
    }
    
    @Override
    public String getName() {
        return "dishRecommend";
    }
    
    @Override
    public String getDescription() {
        return "根据用户喜好推荐菜品";
    }
    
    @Override
    public String getParameterDescription() {
        return "推荐条件（可选）- 可以是菜系（如：川菜、粤菜）、价格区间（如：20-50）、餐食类型（如：早餐、午餐、晚餐）或者空参数获取热门推荐";
    }
    
    @Override
    public String getType() {
        return "dish";
    }
    
    @Override
    public boolean validateParameters(String parameters) {
        // 菜品推荐允许空参数，返回热门推荐
        return true;
    }
    
    @Override
    public ToolResult execute(String parameters) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始推荐菜品，条件：{}", parameters);
            
            RecommendRequest request = parseParameters(parameters);
            List<DishInfo> recommendations = dishRecommendService.recommendDishes(request);
            
            if (recommendations == null || recommendations.isEmpty()) {
                return ToolResult.failure(getName(), 
                        "暂时没有符合条件的菜品推荐，请稍后再试",
                        System.currentTimeMillis() - startTime);
            }
            
            String result = formatRecommendations(recommendations, request);
            
            log.info("菜品推荐成功，推荐{}道菜品", recommendations.size());
            return ToolResult.success(getName(), result, System.currentTimeMillis() - startTime);
            
        } catch (Exception e) {
            log.error("菜品推荐失败，条件：{}", parameters, e);
            return ToolResult.failure(getName(), 
                    "推荐菜品时发生错误，请稍后再试", 
                    System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * 解析推荐参数
     * @param parameters 参数字符串
     * @return 推荐请求对象
     */
    private RecommendRequest parseParameters(String parameters) {
        RecommendRequest request = new RecommendRequest();
        
        if (parameters == null || parameters.trim().isEmpty()) {
            request.setType("popular"); // 默认热门推荐
            return request;
        }
        
        String param = parameters.trim();
        
        // 判断是否为价格区间
        if (param.matches("\\d+-\\d+") || param.matches("\\d+以下") || param.matches("\\d+以上")) {
            request.setType("price");
            request.setPriceRange(param);
        }
        // 判断是否为餐食类型
        else if (param.contains("早餐") || param.contains("午餐") || param.contains("晚餐") || 
                 param.contains("夜宵") || param.contains("下午茶")) {
            request.setType("meal");
            request.setMealType(param);
        }
        // 判断是否为菜系
        else if (param.contains("菜") || param.contains("料理") || param.contains("风味")) {
            request.setType("cuisine");
            request.setCuisine(param);
        }
        // 其他关键词
        else {
            request.setType("keyword");
            request.setKeyword(param);
        }
        
        return request;
    }
    
    /**
     * 格式化推荐结果
     * @param recommendations 推荐菜品列表
     * @param request 推荐请求
     * @return 格式化后的推荐结果
     */
    private String formatRecommendations(List<DishInfo> recommendations, RecommendRequest request) {
        StringBuilder result = new StringBuilder();
        
        // 推荐标题
        result.append(getRecommendTitle(request)).append("\n\n");
        
        // 菜品列表
        for (int i = 0; i < recommendations.size(); i++) {
            DishInfo dish = recommendations.get(i);
            result.append(String.format("%d. 🍽️ **%s**\n", i + 1, dish.getName()));
            result.append(String.format("   💰 价格：¥%.2f\n", dish.getPrice()));
            
            if (dish.getDescription() != null && !dish.getDescription().isEmpty()) {
                result.append(String.format("   📝 简介：%s\n", dish.getDescription()));
            }
            
            if (dish.getRating() != null && dish.getRating() > 0) {
                result.append(String.format("   ⭐ 评分：%.1f分\n", dish.getRating()));
            }
            
            if (dish.getSalesCount() != null && dish.getSalesCount() > 0) {
                result.append(String.format("   🔥 月销量：%d份\n", dish.getSalesCount()));
            }
            
            // 添加标签
            if (dish.getTags() != null && !dish.getTags().isEmpty()) {
                result.append("   🏷️ 标签：");
                for (String tag : dish.getTags()) {
                    result.append(tag).append(" ");
                }
                result.append("\n");
            }
            
            result.append("\n");
        }
        
        // 添加贴心提示
        result.append("💡 贴心提示：\n");
        result.append("- 点击菜品名称可查看详细信息\n");
        result.append("- 部分菜品可能有时令限制\n");
        result.append("- 建议搭配饮品享用更佳\n");
        result.append("- 如需修改菜品配置，请在备注中说明");
        
        return result.toString();
    }
    
    /**
     * 获取推荐标题
     * @param request 推荐请求
     * @return 推荐标题
     */
    private String getRecommendTitle(RecommendRequest request) {
        return switch (request.getType()) {
            case "popular" -> "🔥 热门推荐";
            case "price" -> "💰 价格区间推荐：" + request.getPriceRange();
            case "meal" -> "🍽️ " + request.getMealType() + "推荐";
            case "cuisine" -> "🥘 " + request.getCuisine() + "推荐";
            case "keyword" -> "🔍 \"" + request.getKeyword() + "\" 相关推荐";
            default -> "🍽️ 为您推荐";
        };
    }
    
    /**
     * 菜品推荐服务接口
     */
    public interface DishRecommendService {
        /**
         * 推荐菜品
         * @param request 推荐请求
         * @return 推荐菜品列表
         */
        List<DishInfo> recommendDishes(RecommendRequest request);
    }
    
    /**
     * 推荐请求实体类
     */
    public static class RecommendRequest {
        private String type;        // 推荐类型：popular, price, meal, cuisine, keyword
        private String priceRange;  // 价格区间
        private String mealType;    // 餐食类型
        private String cuisine;     // 菜系
        private String keyword;     // 关键词
        private String userId;      // 用户ID（用于个性化推荐）
        
        // getter和setter方法
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getPriceRange() { return priceRange; }
        public void setPriceRange(String priceRange) { this.priceRange = priceRange; }
        
        public String getMealType() { return mealType; }
        public void setMealType(String mealType) { this.mealType = mealType; }
        
        public String getCuisine() { return cuisine; }
        public void setCuisine(String cuisine) { this.cuisine = cuisine; }
        
        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
    
    /**
     * 菜品信息实体类
     */
    public static class DishInfo {
        private Long id;                    // 菜品ID
        private String name;                // 菜品名称
        private Double price;               // 价格
        private String description;         // 描述
        private String imageUrl;            // 图片URL
        private Double rating;              // 评分
        private Integer salesCount;         // 销量
        private String category;            // 分类
        private List<String> tags;          // 标签
        private Boolean isRecommended;      // 是否推荐
        private Boolean isOnSale;           // 是否在售
        
        // 构造函数
        public DishInfo() {
            this.tags = new ArrayList<>();
        }
        
        // getter和setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        
        public Integer getSalesCount() { return salesCount; }
        public void setSalesCount(Integer salesCount) { this.salesCount = salesCount; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
        
        public Boolean getIsRecommended() { return isRecommended; }
        public void setIsRecommended(Boolean isRecommended) { this.isRecommended = isRecommended; }
        
        public Boolean getIsOnSale() { return isOnSale; }
        public void setIsOnSale(Boolean isOnSale) { this.isOnSale = isOnSale; }
    }
}
