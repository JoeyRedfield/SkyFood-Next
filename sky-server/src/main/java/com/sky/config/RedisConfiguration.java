package com.sky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 * 配置Redis序列化器，解决Java 8时间类型序列化问题
 * 
 * @author wuzy
 * @since 2025-07-14
 */
@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        log.info("开始创建redis模板对象...");
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        //设置redis的连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 使用自定义的ObjectMapper创建JSON序列化器，支持Java 8时间类型
        ObjectMapper objectMapper = new JacksonObjectMapper();
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        //设置redis key的序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(serializer);

        // 如果你是使用 Spring Boot 默认的 RedisAutoConfiguration，它会自动帮你调用这个方法。
        // 但当你自定义 RedisTemplate 的 Bean（比如为了修改序列化器），就需要手动调用它。
        redisTemplate.afterPropertiesSet();
        
        log.info("Redis模板对象创建完成，已配置支持Java 8时间类型的序列化器");
        return redisTemplate;
    }

}
