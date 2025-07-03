package com.sky.service;

import com.sky.dto.AiChatDTO;
import com.sky.vo.AiChatSessionVO;
import com.sky.vo.AiChatVO;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiChatService {

    /**
     * 发送聊天消息
     * @param aiChatDTO
     * @return
     */
    AiChatVO chat(AiChatDTO aiChatDTO);

    /**
     * 流式聊天
     * @param aiChatDTO
     * @return
     */
    Flux<ChatResponse> chatStream(AiChatDTO aiChatDTO);

    /**
     * 获取用户会话列表
     * @return
     */
    List<AiChatSessionVO> getUserSessions();

    /**
     * 获取会话详情
     * @param sessionId
     * @return
     */
    AiChatSessionVO getSessionDetail(String sessionId);

    /**
     * 删除会话
     * @param sessionId
     */
    void deleteSession(String sessionId);
}
