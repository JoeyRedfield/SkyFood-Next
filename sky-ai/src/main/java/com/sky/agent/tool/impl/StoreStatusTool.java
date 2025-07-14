package com.sky.agent.tool.impl;

import com.sky.agent.tool.AgentTool;
import com.sky.agent.tool.ToolResult;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 店铺状态查询工具
 * 查询店铺营业状态、营业时间和基本信息
 * 
 * 功能：
 * 1. 查询店铺当前营业状态
 * 2. 获取营业时间信息
 * 3. 查询配送范围
 * 4. 获取店铺公告信息
 * 5. 查询特殊营业安排
 * 
 * @author wuzy
 * @since 2025-07-14
 */
@Slf4j
public class StoreStatusTool implements AgentTool {
    
    /**
     * 店铺状态服务接口
     */
    private final StoreStatusService storeStatusService;
    
    /**
     * 构造函数
     * @param storeStatusService 店铺状态服务
     */
    public StoreStatusTool(StoreStatusService storeStatusService) {
        this.storeStatusService = storeStatusService;
    }
    
    @Override
    public String getName() {
        return "storeStatus";
    }
    
    @Override
    public String getDescription() {
        return "查询店铺营业状态和基本信息";
    }
    
    @Override
    public String getParameterDescription() {
        return "查询类型（可选）- 可以是'营业时间'、'配送范围'、'店铺公告'或空参数查询当前状态";
    }
    
    @Override
    public String getType() {
        return "store";
    }
    
    @Override
    public boolean validateParameters(String parameters) {
        // 店铺状态查询允许空参数
        return true;
    }
    
    @Override
    public ToolResult execute(String parameters) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始查询店铺状态，查询类型：{}", parameters);
            
            String queryType = parseQueryType(parameters);
            StoreInfo storeInfo = storeStatusService.getStoreInfo();
            
            if (storeInfo == null) {
                return ToolResult.failure(getName(), 
                        "无法获取店铺信息，请稍后再试",
                        System.currentTimeMillis() - startTime);
            }
            
            String result = formatStoreInfo(storeInfo, queryType);
            
            log.info("店铺状态查询成功，查询类型：{}", queryType);
            return ToolResult.success(getName(), result, System.currentTimeMillis() - startTime);
            
        } catch (Exception e) {
            log.error("店铺状态查询失败，参数：{}", parameters, e);
            return ToolResult.failure(getName(), 
                    "查询店铺状态时发生错误，请稍后再试", 
                    System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * 解析查询类型
     * @param parameters 参数字符串
     * @return 查询类型
     */
    private String parseQueryType(String parameters) {
        if (parameters == null || parameters.trim().isEmpty()) {
            return "status"; // 默认查询当前状态
        }
        
        String param = parameters.trim();
        
        if (param.contains("营业时间") || param.contains("时间")) {
            return "hours";
        } else if (param.contains("配送范围") || param.contains("配送") || param.contains("范围")) {
            return "delivery";
        } else if (param.contains("公告") || param.contains("通知") || param.contains("活动")) {
            return "announcement";
        } else if (param.contains("联系") || param.contains("电话") || param.contains("地址")) {
            return "contact";
        } else {
            return "status";
        }
    }
    
    /**
     * 格式化店铺信息
     * @param storeInfo 店铺信息
     * @param queryType 查询类型
     * @return 格式化后的信息
     */
    private String formatStoreInfo(StoreInfo storeInfo, String queryType) {
        StringBuilder result = new StringBuilder();
        
        switch (queryType) {
            case "status" -> result.append(formatStatusInfo(storeInfo));
            case "hours" -> result.append(formatHoursInfo(storeInfo));
            case "delivery" -> result.append(formatDeliveryInfo(storeInfo));
            case "announcement" -> result.append(formatAnnouncementInfo(storeInfo));
            case "contact" -> result.append(formatContactInfo(storeInfo));
            default -> result.append(formatFullInfo(storeInfo));
        }
        
        return result.toString();
    }
    
    /**
     * 格式化当前状态信息
     */
    private String formatStatusInfo(StoreInfo storeInfo) {
        StringBuilder result = new StringBuilder();
        
        result.append("🏪 **苍穹外卖店铺状态**\n\n");
        
        // 当前状态
        String statusIcon = storeInfo.getIsOpen() ? "🟢" : "🔴";
        String statusText = storeInfo.getIsOpen() ? "营业中" : "暂停营业";
        result.append(String.format("%s **当前状态：%s**\n", statusIcon, statusText));
        
        // 当前时间
        result.append(String.format("🕐 当前时间：%s\n", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        
        // 营业时间
        result.append(String.format("⏰ 营业时间：%s\n", storeInfo.getBusinessHours()));
        
        // 特殊提示
        if (!storeInfo.getIsOpen()) {
            result.append("\n⚠️ 当前非营业时间，无法下单。\n");
            if (storeInfo.getNextOpenTime() != null) {
                result.append(String.format("📅 下次营业时间：%s\n", storeInfo.getNextOpenTime()));
            }
        } else {
            result.append("\n✅ 当前可正常下单配送。\n");
            if (storeInfo.getCloseTime() != null) {
                result.append(String.format("⏰ 今日营业至：%s\n", storeInfo.getCloseTime()));
            }
        }
        
        // 配送信息
        result.append(String.format("🚚 配送状态：%s\n", 
                storeInfo.getIsDeliveryAvailable() ? "可配送" : "暂停配送"));
        
        return result.toString();
    }
    
    /**
     * 格式化营业时间信息
     */
    private String formatHoursInfo(StoreInfo storeInfo) {
        StringBuilder result = new StringBuilder();
        
        result.append("⏰ **营业时间信息**\n\n");
        result.append(String.format("🗓️ 正常营业时间：%s\n", storeInfo.getBusinessHours()));
        
        if (storeInfo.getSpecialHours() != null && !storeInfo.getSpecialHours().isEmpty()) {
            result.append("\n📅 特殊营业安排：\n");
            for (String specialHour : storeInfo.getSpecialHours()) {
                result.append(String.format("- %s\n", specialHour));
            }
        }
        
        result.append("\n💡 温馨提示：\n");
        result.append("- 最后下单时间为营业结束前30分钟\n");
        result.append("- 节假日营业时间可能有调整\n");
        result.append("- 具体以当日公告为准");
        
        return result.toString();
    }
    
    /**
     * 格式化配送信息
     */
    private String formatDeliveryInfo(StoreInfo storeInfo) {
        StringBuilder result = new StringBuilder();
        
        result.append("🚚 **配送服务信息**\n\n");
        result.append(String.format("📍 配送范围：%s\n", storeInfo.getDeliveryRange()));
        result.append(String.format("💰 配送费用：%s\n", storeInfo.getDeliveryFee()));
        result.append(String.format("⏱️ 配送时间：%s\n", storeInfo.getDeliveryTime()));
        result.append(String.format("📦 起送金额：¥%.2f\n", storeInfo.getMinOrderAmount()));
        
        if (storeInfo.getFreeDeliveryAmount() != null && storeInfo.getFreeDeliveryAmount() > 0) {
            result.append(String.format("🎁 免配送费：满¥%.2f免配送费\n", storeInfo.getFreeDeliveryAmount()));
        }
        
        result.append("\n💡 配送说明：\n");
        result.append("- 恶劣天气可能影响配送时间\n");
        result.append("- 偏远地区可能不在配送范围内\n");
        result.append("- 如需紧急配送请联系客服");
        
        return result.toString();
    }
    
    /**
     * 格式化公告信息
     */
    private String formatAnnouncementInfo(StoreInfo storeInfo) {
        StringBuilder result = new StringBuilder();
        
        result.append("📢 **店铺公告**\n\n");
        
        if (storeInfo.getAnnouncements() != null && !storeInfo.getAnnouncements().isEmpty()) {
            for (int i = 0; i < storeInfo.getAnnouncements().size(); i++) {
                String announcement = storeInfo.getAnnouncements().get(i);
                result.append(String.format("%d. %s\n", i + 1, announcement));
            }
        } else {
            result.append("暂无最新公告\n");
        }
        
        result.append("\n🎉 优惠活动：\n");
        if (storeInfo.getPromotions() != null && !storeInfo.getPromotions().isEmpty()) {
            for (String promotion : storeInfo.getPromotions()) {
                result.append(String.format("- %s\n", promotion));
            }
        } else {
            result.append("暂无优惠活动\n");
        }
        
        return result.toString();
    }
    
    /**
     * 格式化联系信息
     */
    private String formatContactInfo(StoreInfo storeInfo) {
        StringBuilder result = new StringBuilder();
        
        result.append("📞 **联系方式**\n\n");
        result.append(String.format("🏪 店铺名称：%s\n", storeInfo.getStoreName()));
        result.append(String.format("📍 店铺地址：%s\n", storeInfo.getAddress()));
        result.append(String.format("☎️ 联系电话：%s\n", storeInfo.getPhone()));
        result.append(String.format("🤖 客服热线：%s\n", storeInfo.getCustomerServicePhone()));
        
        if (storeInfo.getEmail() != null && !storeInfo.getEmail().isEmpty()) {
            result.append(String.format("📧 邮箱：%s\n", storeInfo.getEmail()));
        }
        
        result.append("\n🕐 客服服务时间：9:00-21:00\n");
        result.append("💬 也可通过APP内客服功能联系我们");
        
        return result.toString();
    }
    
    /**
     * 格式化完整信息
     */
    private String formatFullInfo(StoreInfo storeInfo) {
        StringBuilder result = new StringBuilder();
        
        result.append(formatStatusInfo(storeInfo)).append("\n\n");
        result.append("📋 更多信息：\n");
        result.append("- 回复'营业时间'查看详细营业时间\n");
        result.append("- 回复'配送范围'查看配送信息\n");
        result.append("- 回复'店铺公告'查看最新公告\n");
        result.append("- 回复'联系方式'查看联系信息");
        
        return result.toString();
    }
    
    /**
     * 店铺状态服务接口
     */
    public interface StoreStatusService {
        /**
         * 获取店铺信息
         * @return 店铺信息
         */
        StoreInfo getStoreInfo();
    }
    
    /**
     * 店铺信息实体类
     */
    public static class StoreInfo {
        private String storeName;                           // 店铺名称
        private Boolean isOpen;                             // 是否营业
        private String businessHours;                       // 营业时间
        private String nextOpenTime;                        // 下次营业时间
        private String closeTime;                           // 今日关店时间
        private Boolean isDeliveryAvailable;                // 是否可配送
        private String deliveryRange;                       // 配送范围
        private String deliveryFee;                         // 配送费用
        private String deliveryTime;                        // 配送时间
        private Double minOrderAmount;                      // 起送金额
        private Double freeDeliveryAmount;                  // 免配送费金额
        private String address;                             // 店铺地址
        private String phone;                               // 店铺电话
        private String customerServicePhone;                // 客服电话
        private String email;                               // 邮箱
        private java.util.List<String> announcements;      // 公告列表
        private java.util.List<String> promotions;         // 优惠活动
        private java.util.List<String> specialHours;       // 特殊营业时间
        
        // getter和setter方法
        public String getStoreName() { return storeName; }
        public void setStoreName(String storeName) { this.storeName = storeName; }
        
        public Boolean getIsOpen() { return isOpen; }
        public void setIsOpen(Boolean isOpen) { this.isOpen = isOpen; }
        
        public String getBusinessHours() { return businessHours; }
        public void setBusinessHours(String businessHours) { this.businessHours = businessHours; }
        
        public String getNextOpenTime() { return nextOpenTime; }
        public void setNextOpenTime(String nextOpenTime) { this.nextOpenTime = nextOpenTime; }
        
        public String getCloseTime() { return closeTime; }
        public void setCloseTime(String closeTime) { this.closeTime = closeTime; }
        
        public Boolean getIsDeliveryAvailable() { return isDeliveryAvailable; }
        public void setIsDeliveryAvailable(Boolean isDeliveryAvailable) { this.isDeliveryAvailable = isDeliveryAvailable; }
        
        public String getDeliveryRange() { return deliveryRange; }
        public void setDeliveryRange(String deliveryRange) { this.deliveryRange = deliveryRange; }
        
        public String getDeliveryFee() { return deliveryFee; }
        public void setDeliveryFee(String deliveryFee) { this.deliveryFee = deliveryFee; }
        
        public String getDeliveryTime() { return deliveryTime; }
        public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }
        
        public Double getMinOrderAmount() { return minOrderAmount; }
        public void setMinOrderAmount(Double minOrderAmount) { this.minOrderAmount = minOrderAmount; }
        
        public Double getFreeDeliveryAmount() { return freeDeliveryAmount; }
        public void setFreeDeliveryAmount(Double freeDeliveryAmount) { this.freeDeliveryAmount = freeDeliveryAmount; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getCustomerServicePhone() { return customerServicePhone; }
        public void setCustomerServicePhone(String customerServicePhone) { this.customerServicePhone = customerServicePhone; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public java.util.List<String> getAnnouncements() { return announcements; }
        public void setAnnouncements(java.util.List<String> announcements) { this.announcements = announcements; }
        
        public java.util.List<String> getPromotions() { return promotions; }
        public void setPromotions(java.util.List<String> promotions) { this.promotions = promotions; }
        
        public java.util.List<String> getSpecialHours() { return specialHours; }
        public void setSpecialHours(java.util.List<String> specialHours) { this.specialHours = specialHours; }
    }
}
