package com.magese.ai.dialogue.llm.tool.function;

import cn.hutool.extra.spring.SpringUtil;
import com.magese.ai.communication.common.ChatSession;
import com.magese.ai.communication.common.SessionManager;
import com.magese.ai.dialogue.llm.ChatService;
import com.magese.ai.dialogue.llm.tool.ToolCallStringResultConverter;
import com.magese.ai.dialogue.llm.tool.ToolsGlobalRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class SessionExitFunction implements ToolsGlobalRegistry.GlobalFunction {


    ToolCallback toolCallback = FunctionToolCallback
            .builder("func_exitSession", (Map<String, String> params, ToolContext toolContext) -> {
                ChatSession chatSession = (ChatSession) toolContext.getContext().get(ChatService.TOOL_CONTEXT_SESSION_KEY);
                SessionManager sessionManager = SpringUtil.getBean(SessionManager.class);
                sessionManager.setCloseAfterChat(chatSession.getSessionId(), true);
                String sayGoodbye = params.get("sayGoodbye");
                if (sayGoodbye == null) {
                    sayGoodbye = "拜拜哟！";
                }
                return sayGoodbye;
            })
            .toolMetadata(ToolMetadata.builder().returnDirect(true).build())
            .description("当用户想结束对话或需要退出时调用function：func_exitSession")
            .inputSchema("""
                        {
                            "type": "object",
                            "properties": {
                                "sayGoodbye": {
                                    "type": "string",
                                    "description": "与用户友好结束对话的告别语"
                                }
                            },
                            "required": ["sayGoodbye"]
                        }
                    """)
            .inputType(Map.class)
            .toolCallResultConverter(ToolCallStringResultConverter.INSTANCE)
            .build();

    @Override
    public ToolCallback getFunctionCallTool(ChatSession chatSession) {
        return toolCallback;
    }
}
