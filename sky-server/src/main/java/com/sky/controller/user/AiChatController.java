package com.sky.controller.user;

import com.sky.dto.AiChatDTO;
import com.sky.result.Result;
import com.sky.service.AiChatService;
import com.sky.vo.AiChatSessionVO;
import com.sky.vo.AiChatVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController("userAiChatController")
@RequestMapping("/user/ai")
@Tag(name = "用户端AI客服接口")
@Slf4j
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    /**
     * 发送聊天消息
     * @param aiChatDTO
     * @return
     */
    @PostMapping("/chat")
    @Operation(summary = "发送聊天消息")
    public Result<AiChatVO> chat(@RequestBody AiChatDTO aiChatDTO) {
        log.info("用户发送AI聊天消息：{}", aiChatDTO);
        AiChatVO chatVO = aiChatService.chat(aiChatDTO);
        return Result.success(chatVO);
    }

    /**
     * 流式聊天
     * @param aiChatDTO
     * @return
     */
    @PostMapping(value = "/chatStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式聊天")
    public Flux<ChatResponse> chatStream(@RequestBody AiChatDTO aiChatDTO) {
        log.info("用户发送流式AI聊天消息：{}", aiChatDTO);
        return aiChatService.chatStream(aiChatDTO);
    }

    /**
     * 获取用户会话列表
     * @return
     */
    @GetMapping("/sessions")
    @Operation(summary = "获取用户会话列表")
    public Result<List<AiChatSessionVO>> getUserSessions() {
        List<AiChatSessionVO> sessions = aiChatService.getUserSessions();
        return Result.success(sessions);
    }

    /**
     * 获取会话详情
     * @param sessionId
     * @return
     */
    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "获取会话详情")
    public Result<AiChatSessionVO> getSessionDetail(@PathVariable String sessionId) {
        AiChatSessionVO sessionVO = aiChatService.getSessionDetail(sessionId);
        return Result.success(sessionVO);
    }

    /**
     * 删除会话
     * @param sessionId
     * @return
     */
    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "删除会话")
    public Result deleteSession(@PathVariable String sessionId) {
        aiChatService.deleteSession(sessionId);
        return Result.success();
    }
}
