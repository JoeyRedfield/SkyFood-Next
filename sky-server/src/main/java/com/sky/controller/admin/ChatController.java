package com.sky.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/admin/ai/basic")
@Tag(name = "基础AI测试接口")
public class ChatController {

    private final ChatModel chatModel;

    @Autowired
    public ChatController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/chat")
    @Operation(summary = "基础聊天测试")
    public Map generate(@RequestParam(value = "message", defaultValue = "你是谁？") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }

    @GetMapping("/generateStream")
    @Operation(summary = "流式聊天测试")
	public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "你是什么模型？") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return this.chatModel.stream(prompt);
    }
}