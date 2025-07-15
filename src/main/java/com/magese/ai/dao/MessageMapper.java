package com.magese.ai.dao;

import com.magese.ai.entity.SysMessage;

import java.util.List;

/**
 * 聊天记录 数据层
 *
 * @author Joey
 */
public interface MessageMapper {

    int add(SysMessage message);

    int delete(SysMessage message);

    List<SysMessage> query(SysMessage message);
}
