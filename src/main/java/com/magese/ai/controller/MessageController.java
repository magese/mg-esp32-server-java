package com.magese.ai.controller;

import com.github.pagehelper.PageInfo;
import com.magese.ai.common.web.AjaxResult;
import com.magese.ai.common.web.PageFilter;
import com.magese.ai.dialogue.llm.ChatService;
import com.magese.ai.entity.SysMessage;
import com.magese.ai.service.SysMessageService;
import com.magese.ai.utils.CmsUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/message")
public class MessageController extends BaseController {

    private final SysMessageService sysMessageService;
    private final ChatService chatService;

    /**
     * 查询对话
     */
    @GetMapping("/query")
    @ResponseBody
    public AjaxResult query(SysMessage message, HttpServletRequest request) {
        try {
            PageFilter pageFilter = initPageFilter(request);
            message.setUserId(CmsUtils.getUserId());
            List<SysMessage> messageList = sysMessageService.query(message, pageFilter);
            AjaxResult result = AjaxResult.success();
            result.put("data", new PageInfo<>(messageList));
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return AjaxResult.error();
        }
    }

    /**
     * 删除聊天记录
     */
    @PostMapping("/delete")
    @ResponseBody
    public AjaxResult delete(SysMessage message) {
        try {

            message.setUserId(CmsUtils.getUserId());
            int rows = sysMessageService.delete(message);
            if (rows > 0) {
                // 删除聊天记录应该清空当前已建立的对话缓存
                chatService.clearMessageCache(message.getDeviceId());
            }
            return AjaxResult.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return AjaxResult.error();
        }
    }

}
