package com.magese.ai.service;

import com.magese.ai.common.web.PageFilter;
import com.magese.ai.entity.SysRole;

import java.util.List;

/**
 * 角色查询/更新
 *
 * @author Joey
 */
public interface SysRoleService {

    /**
     * 添加角色
     */
    int add(SysRole role);

    /**
     * 查询角色信息
     * 指定分页信息
     */
    List<SysRole> query(SysRole role, PageFilter pageFilter);

    /**
     * 更新角色信息
     */
    int update(SysRole role);

    SysRole selectRoleById(Integer roleId);

}
