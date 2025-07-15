package com.magese.ai.dialogue.llm.tool;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工具类型
 */
@Getter
@AllArgsConstructor
public enum ToolType {

    NONE(1, "调用完工具后，不做其他操作"),
    WAIT(2, "调用工具，等待函数返回"),
    CHANGE_SYS_PROMPT(3, "修改系统提示词，切换角色性格或职责"),
    SYSTEM_CTL(4, "系统控制，影响正常的对话流程，如退出、播放音乐等，需要传递conn参数"),
    IOT_CTL(5, "IOT设备控制，需要传递conn参数"),
    MCP_CLIENT(6, "MCP客户端");

    private final int code;
    private final String desc;
}
