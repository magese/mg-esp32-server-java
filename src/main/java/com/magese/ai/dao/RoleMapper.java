package com.magese.ai.dao;

import com.magese.ai.entity.SysRole;

import java.util.List;

/**
 * 角色管理 数据层
 *
 * @author Joey
 */
public interface RoleMapper {
    List<SysRole> query(SysRole role);

    int update(SysRole role);

    int resetDefault(SysRole role);

    int add(SysRole role);

    SysRole selectRoleById(Integer roleId);
}
