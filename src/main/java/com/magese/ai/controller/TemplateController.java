package com.magese.ai.controller;

import com.github.pagehelper.PageInfo;
import com.magese.ai.common.web.AjaxResult;
import com.magese.ai.entity.SysTemplate;
import com.magese.ai.service.SysTemplateService;
import com.magese.ai.utils.CmsUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 提示词模板控制器
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/template")
public class TemplateController {

    private final SysTemplateService templateService;

    /**
     * 查询模板列表
     */
    @GetMapping("/query")
    @ResponseBody
    public AjaxResult query(SysTemplate template) {
        try {
            template.setUserId(CmsUtils.getUserId());
            List<SysTemplate> templateList = templateService.query(template);
            AjaxResult result = AjaxResult.success();
            result.put("data", new PageInfo<>(templateList));
            return result;
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 添加模板
     */
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(SysTemplate template) {
        try {
            template.setUserId(CmsUtils.getUserId());
            int rows = templateService.add(template);
            return rows > 0 ? AjaxResult.success() : AjaxResult.error("添加模板失败");
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 修改模板
     */
    @PostMapping("/update")
    @ResponseBody
    public AjaxResult update(SysTemplate template) {
        try {
            template.setUserId(CmsUtils.getUserId());
            int rows = templateService.update(template);
            return rows > 0 ? AjaxResult.success() : AjaxResult.error("修改模板失败");
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

}
