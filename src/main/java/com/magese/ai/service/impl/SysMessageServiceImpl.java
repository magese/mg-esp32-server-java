package com.magese.ai.service.impl;

import com.github.pagehelper.PageHelper;
import com.magese.ai.common.web.PageFilter;
import com.magese.ai.dao.MessageMapper;
import com.magese.ai.entity.SysMessage;
import com.magese.ai.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 聊天记录
 *
 * @author Joey
 */
@RequiredArgsConstructor
@Service
public class SysMessageServiceImpl extends BaseServiceImpl implements SysMessageService {

    private final MessageMapper messageMapper;

    /**
     * 新增聊天记录
     */
    @Override
    @Transactional(transactionManager = "transactionManager")
    public int add(SysMessage message) {
        return messageMapper.add(message);
    }

    /**
     * 查询聊天记录
     */
    @Override
    public List<SysMessage> query(SysMessage message, PageFilter pageFilter) {
        if (pageFilter != null) {
            PageHelper.startPage(pageFilter.getStart(), pageFilter.getLimit());
        }
        return messageMapper.query(message);
    }

    /**
     * 删除记忆
     */
    @Override
    @Transactional(transactionManager = "transactionManager")
    public int delete(SysMessage message) {
        return messageMapper.delete(message);
    }

}
