package com.sky.config;

import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.interceptor.JwtTokenUserInterceptor;
import com.sky.json.JacksonObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
//import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

//
@Configuration
@Slf4j
public class WebMvc3Configuration implements WebMvcConfigurer {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                        "/admin/employee/login",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/swagger-ui/**",
                        "/doc.html",
                        "/webjars/**"
                );

        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns(
                        "/user/user/login",
                        "/user/shop/status",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/swagger-ui/**",
                        "/doc.html",
                        "/webjars/**"
                );
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("设置静态资源映射...");
        // 这些现在是可选的，Spring Boot 自动配置已恢复
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
//        registry.addResourceHandler("/v3/api-docs/**")
//                .addResourceLocations("classpath:/META-INF/resources/");
    }

    /**
     * 扩展消息转换器,将日期类型从列表转换为时间戳
     * 这个是导致knife4j不能正常显示的罪魁祸首,特别要注意添加的位置
     *
     * @param converters 消息转换器列表
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        //创建一个消息转换器对象
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        //需要为消息转换器设置一个对象转换器，对象转换器可以将Java对象序列化为json数据
        jackson2HttpMessageConverter.setObjectMapper(new JacksonObjectMapper());
        //将自己的消息转化器加入容器中
        converters.add(converters.size() - 1, jackson2HttpMessageConverter);
    }

    // 保留 OpenAPI 配置
    @Bean
    public OpenAPI baseOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("苍穹外卖项目接口文档")
                .version("1.0")
                .description("Springdoc + Knife4j 示例文档"));
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("管理端接口")
                .packagesToScan("com.sky.controller.admin")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("用户端接口")
                .packagesToScan("com.sky.controller.user")
                .build();
    }
}

