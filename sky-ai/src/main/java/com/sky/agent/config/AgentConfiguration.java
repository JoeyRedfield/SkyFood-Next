package com.sky.agent.config;

import com.sky.agent.app.CustomerServiceAgent;
import com.sky.agent.tool.AgentTool;
import com.sky.agent.tool.ToolManager;
import com.sky.agent.tool.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.ArrayList;
import java.util.List;

/**
 * AI智能体配置类
 * 负责智能体、工具和相关服务的配置和初始化
 * 
 * @author wuzy
 * @since 2025-07-14
 */
@Configuration
@Slf4j
public class AgentConfiguration {
    
    /**
     * 配置订单查询工具
     * @return 订单查询工具实例
     */
    @Bean
    public OrderQueryTool orderQueryTool() {
        // 创建模拟的订单查询服务
        OrderQueryTool.OrderQueryService mockOrderService = new MockOrderQueryService();
        return new OrderQueryTool(mockOrderService);
    }
    
    /**
     * 配置菜品推荐工具
     * @return 菜品推荐工具实例
     */
    @Bean
    public DishRecommendTool dishRecommendTool() {
        // 创建模拟的菜品推荐服务
        DishRecommendTool.DishRecommendService mockDishService = new MockDishRecommendService();
        return new DishRecommendTool(mockDishService);
    }
    
    /**
     * 配置店铺状态工具
     * @return 店铺状态工具实例
     */
    @Bean
    public StoreStatusTool storeStatusTool() {
        // 创建模拟的店铺状态服务
        StoreStatusTool.StoreStatusService mockStoreService = new MockStoreStatusService();
        return new StoreStatusTool(mockStoreService);
    }
    
    /**
     * 配置FAQ工具
     * @return FAQ工具实例
     */
    @Bean
    public FAQTool faqTool() {
        // 创建模拟的FAQ服务
        FAQTool.FAQService mockFaqService = new MockFAQService();
        return new FAQTool(mockFaqService);
    }
    
    /**
     * 配置所有工具列表
     * @param orderQueryTool 订单查询工具
     * @param dishRecommendTool 菜品推荐工具
     * @param storeStatusTool 店铺状态工具
     * @param faqTool FAQ工具
     * @return 工具列表
     */
    @Bean
    public List<AgentTool> allAgentTools(
            OrderQueryTool orderQueryTool,
            DishRecommendTool dishRecommendTool,
            StoreStatusTool storeStatusTool,
            FAQTool faqTool) {
        
        List<AgentTool> tools = new ArrayList<>();
        tools.add(orderQueryTool);
        tools.add(dishRecommendTool);
        tools.add(storeStatusTool);
        tools.add(faqTool);
        
        log.info("配置了 {} 个智能体工具", tools.size());
        return tools;
    }
    
    /**
     * 初始化工具管理器
     * @param allAgentTools 所有工具
     * @return 工具管理器
     */
    @Bean
    @DependsOn("allAgentTools")
    public ToolManager toolManager(List<AgentTool> allAgentTools) {
        ToolManager toolManager = ToolManager.getInstance();
        
        // 注册所有工具
        int registeredCount = toolManager.registerTools(allAgentTools);
        log.info("工具管理器初始化完成，成功注册 {} 个工具", registeredCount);
        
        return toolManager;
    }
    
    /**
     * 配置客服智能体
     * @param chatModel 聊天模型
     * @param allAgentTools 所有工具
     * @return 客服智能体实例
     */
    @Bean
    public CustomerServiceAgent customerServiceAgent(
            ChatModel chatModel, 
            List<AgentTool> allAgentTools) {
        
        CustomerServiceAgent agent = new CustomerServiceAgent(chatModel, allAgentTools);
        log.info("苍穹外卖AI客服智能体配置完成");
        
        return agent;
    }
    
    // ============== 模拟服务实现 ==============
    
    /**
     * 模拟订单查询服务
     */
    private static class MockOrderQueryService implements OrderQueryTool.OrderQueryService {
        @Override
        public OrderQueryTool.OrderInfo queryOrderByNumber(String orderNumber) {
            // 模拟订单数据
            OrderQueryTool.OrderInfo orderInfo = new OrderQueryTool.OrderInfo();
            orderInfo.setOrderNumber(orderNumber);
            orderInfo.setStatus(4); // 派送中
            orderInfo.setOrderTime("2025-01-14 12:30:15");
            orderInfo.setAddress("北京市海淀区中关村软件园");
            orderInfo.setPhone("138****5678");
            orderInfo.setAmount(58.50);
            
            // 模拟菜品信息
            List<OrderQueryTool.DishInfo> dishes = new ArrayList<>();
            OrderQueryTool.DishInfo dish1 = new OrderQueryTool.DishInfo();
            dish1.setName("宫保鸡丁");
            dish1.setQuantity(1);
            dish1.setPrice(28.00);
            dishes.add(dish1);
            
            OrderQueryTool.DishInfo dish2 = new OrderQueryTool.DishInfo();
            dish2.setName("蛋炒饭");
            dish2.setQuantity(1);
            dish2.setPrice(18.00);
            dishes.add(dish2);
            
            orderInfo.setDishes(dishes);
            
            // 模拟配送信息
            OrderQueryTool.DeliveryInfo deliveryInfo = new OrderQueryTool.DeliveryInfo();
            deliveryInfo.setDriverName("张师傅");
            deliveryInfo.setDriverPhone("139****1234");
            deliveryInfo.setEstimatedTime("13:15");
            orderInfo.setDeliveryInfo(deliveryInfo);
            
            return orderInfo;
        }
    }
    
    /**
     * 模拟菜品推荐服务
     */
    private static class MockDishRecommendService implements DishRecommendTool.DishRecommendService {
        @Override
        public List<DishRecommendTool.DishInfo> recommendDishes(DishRecommendTool.RecommendRequest request) {
            List<DishRecommendTool.DishInfo> dishes = new ArrayList<>();
            
            // 模拟推荐菜品
            DishRecommendTool.DishInfo dish1 = new DishRecommendTool.DishInfo();
            dish1.setId(1L);
            dish1.setName("麻婆豆腐");
            dish1.setPrice(22.00);
            dish1.setDescription("经典川菜，麻辣鲜香，豆腐嫩滑");
            dish1.setRating(4.8);
            dish1.setSalesCount(1520);
            dish1.setCategory("川菜");
            dish1.getTags().add("招牌菜");
            dish1.getTags().add("下饭神器");
            dishes.add(dish1);
            
            DishRecommendTool.DishInfo dish2 = new DishRecommendTool.DishInfo();
            dish2.setId(2L);
            dish2.setName("回锅肉");
            dish2.setPrice(32.00);
            dish2.setDescription("四川传统菜肴，肥而不腻，香气扑鼻");
            dish2.setRating(4.7);
            dish2.setSalesCount(980);
            dish2.setCategory("川菜");
            dish2.getTags().add("经典菜");
            dish2.getTags().add("家常菜");
            dishes.add(dish2);
            
            DishRecommendTool.DishInfo dish3 = new DishRecommendTool.DishInfo();
            dish3.setId(3L);
            dish3.setName("蒸蛋羹");
            dish3.setPrice(8.00);
            dish3.setDescription("嫩滑如丝，营养丰富，老少皆宜");
            dish3.setRating(4.9);
            dish3.setSalesCount(2100);
            dish3.setCategory("家常菜");
            dish3.getTags().add("养生");
            dish3.getTags().add("清淡");
            dishes.add(dish3);
            
            return dishes;
        }
    }
    
    /**
     * 模拟店铺状态服务
     */
    private static class MockStoreStatusService implements StoreStatusTool.StoreStatusService {
        @Override
        public StoreStatusTool.StoreInfo getStoreInfo() {
            StoreStatusTool.StoreInfo storeInfo = new StoreStatusTool.StoreInfo();
            storeInfo.setStoreName("苍穹外卖（中关村店）");
            storeInfo.setIsOpen(true);
            storeInfo.setBusinessHours("10:00-22:00");
            storeInfo.setCloseTime("22:00");
            storeInfo.setIsDeliveryAvailable(true);
            storeInfo.setDeliveryRange("店铺周边5公里");
            storeInfo.setDeliveryFee("2-8元根据距离计算");
            storeInfo.setDeliveryTime("30-45分钟");
            storeInfo.setMinOrderAmount(20.00);
            storeInfo.setFreeDeliveryAmount(39.00);
            storeInfo.setAddress("北京市海淀区中关村软件园");
            storeInfo.setPhone("010-12345678");
            storeInfo.setCustomerServicePhone("400-8888-888");
            storeInfo.setEmail("service@skydelivery.com");
            
            // 模拟公告
            List<String> announcements = new ArrayList<>();
            announcements.add("新用户首单立减10元！");
            announcements.add("周末全场满减活动进行中");
            storeInfo.setAnnouncements(announcements);
            
            // 模拟优惠活动
            List<String> promotions = new ArrayList<>();
            promotions.add("满39元免配送费");
            promotions.add("每周三会员日8.8折");
            storeInfo.setPromotions(promotions);
            
            return storeInfo;
        }
    }
    
    /**
     * 模拟FAQ服务
     */
    private static class MockFAQService implements FAQTool.FAQService {
        @Override
        public List<FAQTool.FAQItem> searchFAQ(String keyword) {
            List<FAQTool.FAQItem> faqItems = new ArrayList<>();
            
            // 模拟根据关键词返回相关FAQ
            if (keyword.contains("配送") || keyword.contains("送餐")) {
                FAQTool.FAQItem faq = new FAQTool.FAQItem();
                faq.setId(1L);
                faq.setQuestion("配送需要多长时间？");
                faq.setAnswer("正常情况下配送时间为30-45分钟，具体时间会根据距离、天气和订单量有所调整。");
                faq.setCategory("配送问题");
                faqItems.add(faq);
            }
            
            if (keyword.contains("退款")) {
                FAQTool.FAQItem faq = new FAQTool.FAQItem();
                faq.setId(2L);
                faq.setQuestion("如何申请退款？");
                faq.setAnswer("您可以在订单详情页面点击'申请退款'，或联系客服400-8888-888处理。退款一般1-3个工作日内到账。");
                faq.setCategory("退款问题");
                List<String> actions = new ArrayList<>();
                actions.add("联系客服");
                actions.add("查看订单");
                faq.setActions(actions);
                faqItems.add(faq);
            }
            
            return faqItems;
        }
        
        @Override
        public List<FAQTool.FAQItem> fuzzySearchFAQ(String keyword) {
            // 模糊搜索逻辑
            return searchFAQ(keyword);
        }
        
        @Override
        public List<FAQTool.FAQItem> getRelatedQuestions(String keyword) {
            List<FAQTool.FAQItem> related = new ArrayList<>();
            
            FAQTool.FAQItem faq = new FAQTool.FAQItem();
            faq.setId(3L);
            faq.setQuestion("如何联系配送员？");
            faq.setAnswer("订单派送后，您可以在订单详情页面查看配送员电话。");
            faq.setCategory("配送问题");
            related.add(faq);
            
            return related;
        }
        
        @Override
        public List<String> getFAQCategories() {
            List<String> categories = new ArrayList<>();
            categories.add("订单问题");
            categories.add("支付问题");
            categories.add("配送问题");
            categories.add("退款问题");
            categories.add("账户问题");
            return categories;
        }
        
        @Override
        public List<FAQTool.FAQItem> getFAQByCategory(String category) {
            return searchFAQ(category);
        }
    }
}
