package com.sky.test;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.MysqlChatMemoryRepositoryDialect;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.DigestUtils;

public class AiTest {
    @Test
    void test123456(){
        System.out.println(DigestUtils.md5DigestAsHex("123456".getBytes()));
    }
    //    @Autowired
//    JdbcChatMemoryRepository chatMemoryRepository;
//
//    ChatMemory chatMemory = MessageWindowChatMemory.builder()
//            .chatMemoryRepository(chatMemoryRepository)
//            .maxMessages(10)
//            .build();

    JdbcTemplate j = new JdbcTemplate();
    ChatMemoryRepository chatMemoryRepository = JdbcChatMemoryRepository.builder()
            .jdbcTemplate(j)
            .dialect(new MysqlChatMemoryRepositoryDialect())
            .build();

    ChatMemory chatMemory = MessageWindowChatMemory.builder()
            .chatMemoryRepository(chatMemoryRepository)
            .maxMessages(10)
            .build();
}
