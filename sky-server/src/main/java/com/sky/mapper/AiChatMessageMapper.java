package com.sky.mapper;

import com.sky.entity.AiChatMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiChatMessageMapper {

    /**
     * 插入聊天消息
     * @param message
     */
    @Insert("insert into ai_chat_message (session_id, user_id, message_type, content, create_time) " +
            "values (#{sessionId}, #{userId}, #{messageType}, #{content}, #{createTime})")
    void insert(AiChatMessage message);

    /**
     * 根据会话ID查询消息列表
     * @param sessionId
     * @return
     */
    @Select("select * from ai_chat_message where session_id = #{sessionId} order by create_time asc")
    List<AiChatMessage> listBySessionId(String sessionId);

    /**
     * 统计会话消息数量
     * @param sessionId
     * @return
     */
    @Select("select count(*) from ai_chat_message where session_id = #{sessionId}")
    Integer countBySessionId(String sessionId);
}
