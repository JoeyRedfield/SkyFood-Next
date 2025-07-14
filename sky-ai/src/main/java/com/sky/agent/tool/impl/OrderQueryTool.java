package com.sky.agent.tool.impl;

import com.sky.agent.tool.AgentTool;
import com.sky.agent.tool.ToolResult;
import lombok.extern.slf4j.Slf4j;

/**
 * 订单查询工具
 * 根据订单号查询订单状态和详细信息
 * 
 * 功能：
 * 1. 根据订单号查询订单状态
 * 2. 获取订单详细信息
 * 3. 查询配送进度
 * 4. 处理订单相关问题
 * 
 * @author wuzy
 * @since 2025-07-14
 */
@Slf4j
public class OrderQueryTool implements AgentTool {
    
    /**
     * 订单服务接口（这里模拟，实际应注入真实的服务）
     */
    private final OrderQueryService orderQueryService;
    
    /**
     * 构造函数
     * @param orderQueryService 订单查询服务
     */
    public OrderQueryTool(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }
    
    @Override
    public String getName() {
        return "orderQuery";
    }
    
    @Override
    public String getDescription() {
        return "查询订单状态和详细信息";
    }
    
    @Override
    public String getParameterDescription() {
        return "订单号（必须）- 要查询的订单编号，如：202501140001";
    }
    
    @Override
    public String getType() {
        return "order";
    }
    
    @Override
    public boolean validateParameters(String parameters) {
        if (parameters == null || parameters.trim().isEmpty()) {
            return false;
        }
        
        // 检查订单号格式（简单验证：长度8-20位，只包含数字和字母）
        String orderNumber = parameters.trim();
        return orderNumber.length() >= 8 && orderNumber.length() <= 20 
                && orderNumber.matches("^[0-9a-zA-Z]+$");
    }
    
    @Override
    public ToolResult execute(String parameters) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始查询订单：{}", parameters);
            
            String orderNumber = parameters.trim();
            
            // 调用订单查询服务
            OrderInfo orderInfo = orderQueryService.queryOrderByNumber(orderNumber);
            
            if (orderInfo == null) {
                return ToolResult.failure(getName(), 
                        String.format("未找到订单号为 %s 的订单，请检查订单号是否正确", orderNumber),
                        System.currentTimeMillis() - startTime);
            }
            
            // 格式化查询结果
            String result = formatOrderInfo(orderInfo);
            
            log.info("订单查询成功：{}", orderNumber);
            return ToolResult.success(getName(), result, System.currentTimeMillis() - startTime);
            
        } catch (Exception e) {
            log.error("订单查询失败：{}", parameters, e);
            return ToolResult.failure(getName(), 
                    "查询订单时发生错误，请稍后再试或联系客服", 
                    System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * 格式化订单信息
     * @param orderInfo 订单信息
     * @return 格式化后的字符串
     */
    private String formatOrderInfo(OrderInfo orderInfo) {
        StringBuilder result = new StringBuilder();
        
        result.append("📋 订单信息：\n");
        result.append(String.format("订单号：%s\n", orderInfo.getOrderNumber()));
        result.append(String.format("订单状态：%s\n", getStatusDescription(orderInfo.getStatus())));
        result.append(String.format("下单时间：%s\n", orderInfo.getOrderTime()));
        result.append(String.format("配送地址：%s\n", orderInfo.getAddress()));
        result.append(String.format("联系电话：%s\n", maskPhone(orderInfo.getPhone())));
        result.append(String.format("订单金额：¥%.2f\n", orderInfo.getAmount()));
        
        // 添加菜品信息
        if (orderInfo.getDishes() != null && !orderInfo.getDishes().isEmpty()) {
            result.append("\n🍽️ 菜品明细：\n");
            for (DishInfo dish : orderInfo.getDishes()) {
                result.append(String.format("- %s x%d  ¥%.2f\n", 
                        dish.getName(), dish.getQuantity(), dish.getPrice()));
            }
        }
        
        // 添加配送信息
        if (orderInfo.getDeliveryInfo() != null) {
            result.append("\n🚚 配送信息：\n");
            DeliveryInfo delivery = orderInfo.getDeliveryInfo();
            result.append(String.format("配送员：%s\n", delivery.getDriverName()));
            result.append(String.format("联系电话：%s\n", maskPhone(delivery.getDriverPhone())));
            result.append(String.format("预计送达：%s\n", delivery.getEstimatedTime()));
        }
        
        // 添加状态建议
        result.append("\n").append(getStatusAdvice(orderInfo.getStatus()));
        
        return result.toString();
    }
    
    /**
     * 获取状态描述
     * @param status 状态码
     * @return 状态描述
     */
    private String getStatusDescription(Integer status) {
        return switch (status) {
            case 1 -> "待付款 💰";
            case 2 -> "待接单 📝";
            case 3 -> "已接单 ✅";
            case 4 -> "派送中 🚚";
            case 5 -> "已完成 ✨";
            case 6 -> "已取消 ❌";
            case 7 -> "已退款 💸";
            default -> "未知状态";
        };
    }
    
    /**
     * 获取状态建议
     * @param status 状态码
     * @return 建议文本
     */
    private String getStatusAdvice(Integer status) {
        return switch (status) {
            case 1 -> "💡 建议：请尽快完成支付，避免订单超时取消。";
            case 2 -> "💡 建议：商家正在确认订单，请耐心等待。如超过10分钟未接单，可联系客服。";
            case 3 -> "💡 建议：商家已开始制作，预计20-30分钟完成制作。";
            case 4 -> "💡 建议：配送员正在路上，请保持电话畅通，注意查收。";
            case 5 -> "💡 感谢您的使用，欢迎对订单进行评价！";
            case 6 -> "💡 订单已取消，如有问题请联系客服。";
            case 7 -> "💡 退款已处理，1-3个工作日内到账。";
            default -> "💡 如有疑问，请联系客服：400-8888-888";
        };
    }
    
    /**
     * 手机号脱敏
     * @param phone 原始手机号
     * @return 脱敏后的手机号
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        
        if (phone.length() == 11) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        }
        
        return phone.substring(0, 3) + "****";
    }
    
    /**
     * 订单查询服务接口
     */
    public interface OrderQueryService {
        /**
         * 根据订单号查询订单
         * @param orderNumber 订单号
         * @return 订单信息
         */
        OrderInfo queryOrderByNumber(String orderNumber);
    }
    
    /**
     * 订单信息实体类
     */
    public static class OrderInfo {
        private String orderNumber;
        private Integer status;
        private String orderTime;
        private String address;
        private String phone;
        private Double amount;
        private java.util.List<DishInfo> dishes;
        private DeliveryInfo deliveryInfo;
        
        // getter和setter方法
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        
        public String getOrderTime() { return orderTime; }
        public void setOrderTime(String orderTime) { this.orderTime = orderTime; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        
        public java.util.List<DishInfo> getDishes() { return dishes; }
        public void setDishes(java.util.List<DishInfo> dishes) { this.dishes = dishes; }
        
        public DeliveryInfo getDeliveryInfo() { return deliveryInfo; }
        public void setDeliveryInfo(DeliveryInfo deliveryInfo) { this.deliveryInfo = deliveryInfo; }
    }
    
    /**
     * 菜品信息实体类
     */
    public static class DishInfo {
        private String name;
        private Integer quantity;
        private Double price;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }
    
    /**
     * 配送信息实体类
     */
    public static class DeliveryInfo {
        private String driverName;
        private String driverPhone;
        private String estimatedTime;
        
        public String getDriverName() { return driverName; }
        public void setDriverName(String driverName) { this.driverName = driverName; }
        
        public String getDriverPhone() { return driverPhone; }
        public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }
        
        public String getEstimatedTime() { return estimatedTime; }
        public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }
    }
}
