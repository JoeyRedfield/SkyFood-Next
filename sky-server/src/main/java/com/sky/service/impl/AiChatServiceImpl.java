package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.AiChatDTO;
import com.sky.entity.AiChatMessage;
import com.sky.entity.AiChatSession;
import com.sky.mapper.AiChatMessageMapper;
import com.sky.mapper.AiChatSessionMapper;
import com.sky.service.AiChatService;
import com.sky.vo.AiChatSessionVO;
import com.sky.vo.AiChatVO;
import com.sky.vo.AiMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AiChatServiceImpl implements AiChatService {

    @Autowired
    private ChatModel chatModel;
    @Autowired
    private AiChatSessionMapper aiChatSessionMapper;
    @Autowired
    private AiChatMessageMapper aiChatMessageMapper;

    /**
     * 发送聊天消息
     * @param aiChatDTO
     * @return
     */
    @Override
    @Transactional
    public AiChatVO chat(AiChatDTO aiChatDTO) {
        Long userId = BaseContext.getCurrentId();
        String sessionId = aiChatDTO.getSessionId();

        // 1. 如果没有会话ID，创建新会话
        if (!StringUtils.hasText(sessionId)) {
            sessionId = createNewSession(userId, aiChatDTO.getMessage());
        }

        // 2. 保存用户消息
        saveMessage(sessionId, userId, "user", aiChatDTO.getMessage());

        // 3. 获取会话历史消息构建上下文
        List<Message> messages = buildContextMessages(sessionId);

        // 4. 添加系统提示词，指导AI角色
        String systemPrompt = "你是苍穹外卖的AI客服助手，请友好、专业地回答用户关于外卖、菜品、订单等相关问题。";
        List<Message> allMessages = new ArrayList<>();
        allMessages.add(new UserMessage(systemPrompt)); // 使用UserMessage包装系统提示
        allMessages.addAll(messages);

        // 5. 调用AI模型
        Prompt prompt = new Prompt(allMessages);
//        String reply = chatModel.call(prompt).getResult().getOutput().getContent();
        String reply = chatModel.call(prompt).getResult().getOutput().getText();

        // 6. 保存AI回复
        saveMessage(sessionId, userId, "assistant", reply);

        // 7. 更新会话的最后活跃时间
        updateSessionTime(sessionId);

        // 8. 构建并返回响应
        return AiChatVO.builder()
                .sessionId(sessionId)
                .reply(reply)
                .responseTime(LocalDateTime.now())
                .messageCount(aiChatMessageMapper.countBySessionId(sessionId))
                .build();
    }

    /**
     * 流式聊天
     * @param aiChatDTO
     * @return
     */
    @Override
    public Flux<ChatResponse> chatStream(AiChatDTO aiChatDTO) {
        Long userId = BaseContext.getCurrentId();
        String sessionId = aiChatDTO.getSessionId();

        // 如果没有会话ID，创建新会话
        if (!StringUtils.hasText(sessionId)) {
            sessionId = createNewSession(userId, aiChatDTO.getMessage());
        }

        // 保存用户消息
        saveMessage(sessionId, userId, "user", aiChatDTO.getMessage());

        // 获取会话历史消息构建上下文
        List<Message> messages = buildContextMessages(sessionId);

        // 添加系统提示词
        String systemPrompt = "你是苍穹外卖的AI客服助手，请友好、专业地回答用户关于外卖、菜品、订单等相关问题。";
        List<Message> allMessages = new ArrayList<>();
        allMessages.add(new UserMessage(systemPrompt));
        allMessages.addAll(messages);

        // 流式调用AI模型，直接返回Flux<ChatResponse>
        return chatModel.stream(new Prompt(allMessages));
    }

    /**
     * 获取用户会话列表
     * @return
     */
    @Override
    public List<AiChatSessionVO> getUserSessions() {
        Long userId = BaseContext.getCurrentId();
        List<AiChatSession> sessions = aiChatSessionMapper.listByUserId(userId);

        List<AiChatSessionVO> sessionVOs = new ArrayList<>();
        for (AiChatSession session : sessions) {
            AiChatSessionVO sessionVO = new AiChatSessionVO();
            BeanUtils.copyProperties(session, sessionVO);
            sessionVOs.add(sessionVO);
        }

        return sessionVOs;
    }

    /**
     * 获取会话详情
     * @param sessionId
     * @return
     */
    @Override
    public AiChatSessionVO getSessionDetail(String sessionId) {
        AiChatSession session = aiChatSessionMapper.getBySessionId(sessionId);
        List<AiChatMessage> messages = aiChatMessageMapper.listBySessionId(sessionId);

        AiChatSessionVO sessionVO = new AiChatSessionVO();
        BeanUtils.copyProperties(session, sessionVO);

        List<AiMessageVO> messageVOs = new ArrayList<>();
        for (AiChatMessage message : messages) {
            AiMessageVO messageVO = new AiMessageVO();
            BeanUtils.copyProperties(message, messageVO);
            messageVOs.add(messageVO);
        }
        sessionVO.setMessages(messageVOs);

        return sessionVO;
    }

    /**
     * 删除会话
     * @param sessionId
     */
    @Override
    @Transactional
    public void deleteSession(String sessionId) {
        aiChatSessionMapper.deleteBySessionId(sessionId);
    }

    /**
     * 创建新会话
     * @param userId
     * @param firstMessage
     * @return
     */
    private String createNewSession(Long userId, String firstMessage) {
        String sessionId = UUID.randomUUID().toString();
        String title = generateSessionTitle(firstMessage);

        AiChatSession session = AiChatSession.builder()
                .userId(userId)
                .sessionId(sessionId)
                .title(title)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .build();

        aiChatSessionMapper.insert(session);
        return sessionId;
    }

    /**
     * 生成会话标题
     * @param firstMessage
     * @return
     */
    private String generateSessionTitle(String firstMessage) {
        // 简单的标题生成逻辑，可以根据需要优化
        if (firstMessage.length() > 20) {
            return firstMessage.substring(0, 20) + "...";
        }
        return firstMessage;
    }

    /**
     * 保存消息
     * @param sessionId
     * @param userId
     * @param messageType
     * @param content
     */
    private void saveMessage(String sessionId, Long userId, String messageType, String content) {
        AiChatMessage message = AiChatMessage.builder()
                .sessionId(sessionId)
                .userId(userId)
                .messageType(messageType)
                .content(content)
                .createTime(LocalDateTime.now())
                .build();

        aiChatMessageMapper.insert(message);
    }

    /**
     * 构建上下文消息列表
     * @param sessionId
     * @return
     */
    private List<Message> buildContextMessages(String sessionId) {
        List<AiChatMessage> chatMessages = aiChatMessageMapper.listBySessionId(sessionId);
        List<Message> messages = new ArrayList<>();

        // 限制上下文长度，只保留最近的10条消息
        int startIndex = Math.max(0, chatMessages.size() - 10);
        for (int i = startIndex; i < chatMessages.size(); i++) {
            AiChatMessage chatMessage = chatMessages.get(i);
            if ("user".equals(chatMessage.getMessageType())) {
                messages.add(new UserMessage(chatMessage.getContent()));
            } else if ("assistant".equals(chatMessage.getMessageType())) {
                messages.add(new AssistantMessage(chatMessage.getContent()));
            }
        }

        return messages;
    }

    /**
     * 更新会话时间
     * @param sessionId
     */
    private void updateSessionTime(String sessionId) {
        AiChatSession session = aiChatSessionMapper.getBySessionId(sessionId);
        if (session != null) {
            session.setUpdateTime(LocalDateTime.now());
            aiChatSessionMapper.update(session);
        }
    }
}
