package com.magese.ai.dialogue.llm.tool.function;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.magese.ai.communication.common.ChatSession;
import com.magese.ai.dialogue.llm.ChatService;
import com.magese.ai.dialogue.llm.tool.ToolCallStringResultConverter;
import com.magese.ai.dialogue.llm.tool.ToolsGlobalRegistry;
import com.magese.ai.dialogue.service.HuiBenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class PlayHuiBenFunction implements ToolsGlobalRegistry.GlobalFunction {

    ToolCallback toolCallback = FunctionToolCallback
            .builder("func_playHuiBen", (Map<String, String> params, ToolContext toolContext) -> {
                ChatSession chatSession = (ChatSession) toolContext.getContext().get(ChatService.TOOL_CONTEXT_SESSION_KEY);
                Integer num = MapUtil.getInt(params, "num");
                try {
                    if (num == null || num < 5 || num > 1100) {
                        num = RandomUtil.randomInt(5, 1100);
                    }
                    HuiBenService huiBenService = SpringUtil.getBean(HuiBenService.class);
                    huiBenService.playHuiBen(chatSession, num);
                    return "尝试播放绘本《" + num + "》";

                } catch (Exception e) {
                    log.error("播放绘本异常，绘本编号: {}", num, e);
                    return "绘本播放失败";
                }
            })
            .toolMetadata(ToolMetadata.builder().returnDirect(true).build())
            .description("绘本播放助手，需要用户提供绘本数字编号")
            .inputSchema("""
                        {
                            "type": "object",
                            "properties": {
                                "num": {
                                    "type": "integer",
                                    "description": "要播放的绘本数字编号"
                                }
                            },
                            "required": ["num"]
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
