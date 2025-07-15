package com.magese.ai.dialogue.llm.memory;

import com.magese.ai.entity.SysDevice;
import com.magese.ai.entity.SysRole;

public interface ConversationFactory {
    /**
     * 不同的ChatMemory实现类，可以有不同的处理策略，可以初始化不同的Conversation子类。
     *
     * @param device    设备
     * @param role      角色
     * @param sessionId 会话ID
     * @return 会话
     */
    Conversation initConversation(SysDevice device, SysRole role, String sessionId);
}
