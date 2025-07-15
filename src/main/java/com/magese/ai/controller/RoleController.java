package com.magese.ai.controller;

import com.github.pagehelper.PageInfo;
import com.magese.ai.common.web.AjaxResult;
import com.magese.ai.common.web.PageFilter;
import com.magese.ai.dialogue.tts.factory.TtsServiceFactory;
import com.magese.ai.entity.SysConfig;
import com.magese.ai.entity.SysRole;
import com.magese.ai.service.SysConfigService;
import com.magese.ai.service.SysRoleService;
import com.magese.ai.utils.CmsUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理
 *
 * @author Joey
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/role")
public class RoleController extends BaseController {

    private final SysRoleService roleService;
    private final TtsServiceFactory ttsService;
    private final SysConfigService configService;

    /**
     * 角色查询
     */
    @GetMapping("/query")
    @ResponseBody
    public AjaxResult query(SysRole role, HttpServletRequest request) {
        try {
            PageFilter pageFilter = initPageFilter(request);
            role.setUserId(CmsUtils.getUserId());
            List<SysRole> roleList = roleService.query(role, pageFilter);
            AjaxResult result = AjaxResult.success();
            result.put("data", new PageInfo<>(roleList));
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return AjaxResult.error();
        }
    }

    /**
     * 角色信息更新
     */
    @PostMapping("/update")
    @ResponseBody
    public AjaxResult update(SysRole role) {
        try {
            role.setUserId(CmsUtils.getUserId());
            roleService.update(role);
            return AjaxResult.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return AjaxResult.error();
        }
    }

    /**
     * 添加角色
     */
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(SysRole role) {
        try {
            role.setUserId(CmsUtils.getUserId());
            roleService.add(role);
            return AjaxResult.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return AjaxResult.error();
        }
    }

    @GetMapping("/testVoice")
    @ResponseBody
    public AjaxResult testAudio(String message, String provider, Integer ttsId, String voiceName) {
        SysConfig config = null;
        try {
            if (!provider.equals("edge")) {
                config = configService.selectConfigById(ttsId);
            }
            String audioFilePath = ttsService.getTtsService(config, voiceName).textToSpeech(message);
            AjaxResult result = AjaxResult.success();
            result.put("data", audioFilePath);
            return result;
        } catch (IndexOutOfBoundsException e) {
            return AjaxResult.error("请先到语音合成配置页面配置对应Key");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return AjaxResult.error();
        }
    }
}
