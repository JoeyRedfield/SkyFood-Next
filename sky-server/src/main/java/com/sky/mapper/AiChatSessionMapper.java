package com.sky.mapper;

import com.sky.entity.AiChatSession;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AiChatSessionMapper {

    /**
     * 插入聊天会话
     * @param session
     */
    @Insert("insert into ai_chat_session (user_id, session_id, title, create_time, update_time, is_deleted) " +
            "values (#{userId}, #{sessionId}, #{title}, #{createTime}, #{updateTime}, #{isDeleted})")
    void insert(AiChatSession session);

    /**
     * 根据会话ID查询会话信息
     * @param sessionId
     * @return
     */
    @Select("select * from ai_chat_session where session_id = #{sessionId} and is_deleted = 0")
    AiChatSession getBySessionId(String sessionId);

    /**
     * 根据用户ID查询会话列表
     * @param userId
     * @return
     */
    @Select("select * from ai_chat_session where user_id = #{userId} and is_deleted = 0 order by update_time desc")
    List<AiChatSession> listByUserId(Long userId);

    /**
     * 更新会话信息
     * @param session
     */
    @Update("update ai_chat_session set title = #{title}, update_time = #{updateTime} where session_id = #{sessionId}")
    void update(AiChatSession session);

    /**
     * 删除会话
     * @param sessionId
     */
    @Update("update ai_chat_session set is_deleted = 1 where session_id = #{sessionId}")
    void deleteBySessionId(String sessionId);
}
