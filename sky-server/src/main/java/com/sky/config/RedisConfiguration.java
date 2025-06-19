package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        log.info("开始创建redis模板对象...");
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        //设置redis的连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 使用 JSON 序列化
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

        //设置redis key的序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(serializer);

        // 如果你是使用 Spring Boot 默认的 RedisAutoConfiguration，它会自动帮你调用这个方法。
        // 但当你自定义 RedisTemplate 的 Bean（比如为了修改序列化器），就需要手动调用它。
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

}
