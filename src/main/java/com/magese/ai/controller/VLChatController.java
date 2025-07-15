package com.magese.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magese.ai.common.interceptor.UnLogin;
import com.magese.ai.communication.common.ChatSession;
import com.magese.ai.communication.common.SessionManager;
import com.magese.ai.dialogue.llm.factory.ChatModelFactory;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 视觉对话
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class VLChatController extends BaseController {

    @Resource
    private ChatModelFactory chatModelFactory;

    @Resource
    private SessionManager sessionManager;


    /**
     * 视觉对话
     */
    @UnLogin
    @PostMapping(value = "/vl/chat", produces = "application/json;charset=UTF-8")
    public String vlChat(@RequestParam("file") MultipartFile file,
                         @RequestParam String question,
                         HttpServletRequest request) {
        try {
            //获取当前下发的session信息
            String authorization = request.getHeader("authorization");
            log.info("用户authorization：{}", authorization);
            //下发的是session
            String sessionId = authorization.substring(7);
            ChatSession session = sessionManager.getSession(sessionId);
            if (session == null) {
                return "session不存在";
            }

            ChatModel chatModel = chatModelFactory.takeVisionModel();

            MimeType mimeType = MimeType.valueOf(Objects.requireNonNull(file.getContentType()));
            Media media = Media.builder()
                    .mimeType(mimeType)
                    .data(file.getResource())
                    .build();

            UserMessage userMessage = UserMessage.builder()
                    .media(media)
                    .text(question)
                    .build();
            String call = chatModel.call(userMessage);
            log.info("问题：{}，图文识别内容：{}", question, call);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("text", call);
            String string = new ObjectMapper().writeValueAsString(result);
            log.info("json结果:{}", string);

            return string;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "无可以使用的视觉模型";
        }
    }
}
