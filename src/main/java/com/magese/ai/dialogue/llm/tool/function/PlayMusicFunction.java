package com.magese.ai.dialogue.llm.tool.function;

import cn.hutool.extra.spring.SpringUtil;
import com.magese.ai.communication.common.ChatSession;
import com.magese.ai.dialogue.llm.ChatService;
import com.magese.ai.dialogue.llm.tool.ToolCallStringResultConverter;
import com.magese.ai.dialogue.llm.tool.ToolsGlobalRegistry;
import com.magese.ai.dialogue.service.MusicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class PlayMusicFunction implements ToolsGlobalRegistry.GlobalFunction {

    ToolCallback toolCallback = FunctionToolCallback
            .builder("func_playMusic", (Map<String, String> params, ToolContext toolContext) -> {
                ChatSession chatSession = (ChatSession) toolContext.getContext().get(ChatService.TOOL_CONTEXT_SESSION_KEY);
                String songName = params.get("songName");
                try {
                    if (songName == null || songName.isEmpty()) {
                        return "音乐播放失败";
                    } else {
                        MusicService musicService = SpringUtil.getBean(MusicService.class);
                        musicService.playMusic(chatSession, songName, null);
                        return "尝试播放歌曲《" + songName + "》";
                    }
                } catch (Exception e) {
                    log.error("device 音乐播放异常，song name: {}", songName, e);
                    return "音乐播放失败";
                }
            })
            .toolMetadata(ToolMetadata.builder().returnDirect(true).build())
            .description("音乐播放助手,需要用户提供歌曲的名称")
            .inputSchema("""
                        {
                            "type": "object",
                            "properties": {
                                "songName": {
                                    "type": "string",
                                    "description": "要播放的歌曲名称"
                                }
                            },
                            "required": ["songName"]
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
