package com.magese.ai.dialogue.llm.providers;

import io.github.imfangs.dify.client.DifyChatClient;
import io.github.imfangs.dify.client.DifyClientFactory;
import io.github.imfangs.dify.client.callback.ChatStreamCallback;
import io.github.imfangs.dify.client.enums.ResponseMode;
import io.github.imfangs.dify.client.event.ErrorEvent;
import io.github.imfangs.dify.client.event.MessageEndEvent;
import io.github.imfangs.dify.client.event.MessageEvent;
import io.github.imfangs.dify.client.model.chat.ChatMessage;
import io.github.imfangs.dify.client.model.chat.ChatMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class DifyChatModel implements ChatModel {

    private final DifyChatClient chatClient;

    /**
     * 构造函数
     *
     * @param endpoint API端点
     * @param apiKey   API密钥
     */
    public DifyChatModel(String endpoint, String apiKey) {
        chatClient = DifyClientFactory.createChatClient(endpoint, apiKey);
    }

    public String getProviderName() {
        return "dify";
    }

    @Override
    public ChatResponse call(Prompt prompt) {

        // 创建聊天消息
        ChatMessage message = ChatMessage.builder()
                .query(prompt.getContents())
                .user("user-123") // TODO 用户ID,通过Options传入。
                .responseMode(ResponseMode.BLOCKING)
                .build();
        try {
            // 发送消息并获取响应
            ChatMessageResponse response = chatClient.sendChatMessage(message);
            log.debug("回复: {}", response.getAnswer());
            log.debug("会话ID: {}", response.getConversationId());
            log.debug("消息ID: {}", response.getMessageId());
            return new ChatResponse(List.of(new Generation(new AssistantMessage(response.getAnswer(), Map.of("messageId", response.getMessageId(), "conversationId", response.getConversationId())))));

        } catch (IOException e) {
            log.error("错误: ", e);
            return ChatResponse.builder().generations(Collections.emptyList()).build();
        }

    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return Flux.create(sink -> {

            // 创建唯一的用户ID. TODO 用户ID,通过Options传入。
            String userId = "user_xz_" + UUID.randomUUID().toString().replace("-", "");

            ChatMessage message = ChatMessage.builder()
                    .user(userId)
                    .query(prompt.getUserMessage().getText())
                    .responseMode(ResponseMode.STREAMING)
                    .build();

            // 发送流式消息
            try {
                chatClient.sendChatMessageStream(message, new ChatStreamCallback() {
                    @Override
                    public void onMessage(MessageEvent event) {
                        sink.next(ChatResponse.builder().generations(
                                        List.of(new Generation(new AssistantMessage(event.getAnswer(),
                                                Map.of("messageId", event.getMessageId(),
                                                        "conversationId", event.getConversationId())))))
                                .build());
                    }


                    @Override
                    public void onMessageEnd(MessageEndEvent event) {
                        // 通知完成
                        sink.complete();
                    }

                    @Override
                    public void onError(ErrorEvent event) {
                        sink.error(new IOException(event.toString()));
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        log.error("异常: {}", throwable.getMessage());
                        sink.error(throwable);
                    }

                });
            } catch (IOException e) {
                sink.error(e);
            }
        });
    }
}
