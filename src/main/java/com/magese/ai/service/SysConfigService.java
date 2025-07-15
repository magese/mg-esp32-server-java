package com.magese.ai.service;

import com.magese.ai.common.web.PageFilter;
import com.magese.ai.entity.SysConfig;

import java.util.List;

/**
 * 配置
 *
 * @author Joey
 */
public interface SysConfigService {

    /**
     * 添加配置
     */
    int add(SysConfig config);

    /**
     * 修改配置
     */
    int update(SysConfig config);

    /**
     * 查询
     */
    List<SysConfig> query(SysConfig config, PageFilter pageFilter);

    /**
     * 查询配置
     */
    SysConfig selectConfigById(Integer configId);

    /**
     * 查询默认配置
     */
    SysConfig selectModelType(String modelType);
}
