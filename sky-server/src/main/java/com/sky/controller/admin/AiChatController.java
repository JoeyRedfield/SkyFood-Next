package com.sky.controller.admin;

import com.sky.dto.AiChatDTO;
import com.sky.result.Result;
import com.sky.service.AiChatService;
import com.sky.vo.AiChatVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController("adminAiChatController")
@RequestMapping("/admin/ai")
@Tag(name = "管理端AI客服接口")
@Slf4j
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    /**
     * 管理员测试AI聊天
     * @param aiChatDTO
     * @return
     */
    @PostMapping("/test")
    @Operation(summary = "管理员测试AI聊天")
    public Result<AiChatVO> testChat(@RequestBody AiChatDTO aiChatDTO) {
        log.info("管理员测试AI聊天：{}", aiChatDTO);
        AiChatVO chatVO = aiChatService.chat(aiChatDTO);
        return Result.success(chatVO);
    }

    /**
     * 管理员测试流式聊天
     * @param aiChatDTO
     * @return
     */
    @PostMapping(value = "/testStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "管理员测试流式聊天")
    public Flux<ChatResponse> testChatStream(@RequestBody AiChatDTO aiChatDTO) {
        log.info("管理员测试流式AI聊天：{}", aiChatDTO);
        return aiChatService.chatStream(aiChatDTO);
    }
}
