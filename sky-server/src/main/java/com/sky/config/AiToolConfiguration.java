package com.sky.config;

import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
@Slf4j
public class AiToolConfiguration {

    @Bean
    @Description("根据订单号查询订单状态")
    public Function<OrderTool.Request, OrderTool.Response> orderStatusTool(OrderService orderService) {
        return new OrderTool(orderService);
    }

    // 未来可以添加更多工具，例如：
    // @Bean
    // @Description("查询菜品信息")
    // public Function<DishTool.Request, DishTool.Response> dishSearchTool(DishService dishService) {
    //     return new DishTool(dishService);
    // }
}

/**
 * 订单状态查询工具
 */
@Slf4j
class OrderTool implements Function<OrderTool.Request, OrderTool.Response> {

    private final OrderService orderService;

    public OrderTool(OrderService orderService) {
        this.orderService = orderService;
    }

    // AI 请求时使用的参数
    public record Request(
            @Description("要查询的订单号") String orderNumber
    ) {}

    // 返回给 AI 的响应
    public record Response(String status, String description) {}

    @Override
    public Response apply(Request request) {
        log.info("AI 工具调用：查询订单状态，订单号: {}", request.orderNumber());
        try {
            // 注意：这里的实现需要根据您 OrderService 的具体方法进行调整
            // 这是一个示例，您需要实现一个通过订单号查询订单详情的业务方法
            // Order order = orderService.getByNumber(request.orderNumber());
            // if (order != null) {
            //     String statusText = convertStatusToString(order.getStatus());
            //     return new Response(statusText, "订单状态查询成功");
            // }
            // return new Response("未找到订单", "未查询到该订单号对应的订单");

            // 临时的模拟实现
            if ("123456789".equals(request.orderNumber())) {
                return new Response("派送中", "您的订单正在飞速派送中！");
            }
            return new Response("未找到订单", "系统里没有找到这个订单哦");

        } catch (Exception e) {
            log.error("订单状态查询工具执行异常", e);
            return new Response("查询异常", "查询订单时出现内部错误");
        }
    }
}
