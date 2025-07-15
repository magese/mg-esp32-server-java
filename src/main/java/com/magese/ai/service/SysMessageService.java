package com.magese.ai.service;

import com.magese.ai.common.web.PageFilter;
import com.magese.ai.entity.SysMessage;

import java.util.List;

/**
 * 聊天记录查询/添加
 *
 * @author Joey
 */
public interface SysMessageService {

    /**
     * 新增记录
     */
    int add(SysMessage message);

    /**
     * 查询聊天记录
     * 指定分页信息
     */
    List<SysMessage> query(SysMessage message, PageFilter pageFilter);

    /**
     * 删除记忆
     */
    int delete(SysMessage message);

}
