package com.magese.ai.service.impl;

import com.github.pagehelper.PageHelper;
import com.magese.ai.common.web.PageFilter;
import com.magese.ai.dao.RoleMapper;
import com.magese.ai.entity.SysRole;
import com.magese.ai.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色操作
 *
 * @author Joey
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysRoleServiceImpl extends BaseServiceImpl implements SysRoleService {

    private final static String CACHE_NAME = "Magese:SysRole";

    private final RoleMapper roleMapper;
    private final CacheManager cacheManager;

    /**
     * 添加角色
     */
    @Override
    @Transactional(transactionManager = "transactionManager")
    public int add(SysRole role) {
        // 如果当前配置被设置为默认，则将同类型同用户的其他配置设置为非默认
        if (role.getIsDefault() != null && role.getIsDefault().equals("1")) {
            roleMapper.resetDefault(role);
        }
        // 添加角色
        return roleMapper.add(role);
    }

    /**
     * 查询角色信息
     * 指定分页信息
     */
    @Override
    public List<SysRole> query(SysRole role, PageFilter pageFilter) {
        if (pageFilter != null) {
            PageHelper.startPage(pageFilter.getStart(), pageFilter.getLimit());
        }
        return roleMapper.query(role);
    }

    /**
     * 更新角色信息
     */
    @Override
    @Transactional(transactionManager = "transactionManager")
    public int update(SysRole role) {
        // 如果当前配置被设置为默认，则将同类型同用户的其他配置设置为非默认
        if (role.getIsDefault() != null && role.getIsDefault().equals("1")) {
            roleMapper.resetDefault(role);
        }

        int result = roleMapper.update(role);

        // 如果更新成功且roleId不为空，直接将更新后的完整对象加载到缓存中
        if (result > 0 && role.getRoleId() != null && cacheManager != null) {
            // 直接从数据库查询最新数据
            SysRole updatedRole = roleMapper.selectRoleById(role.getRoleId());
            // 手动更新缓存
            if (updatedRole != null) {
                Cache cache = cacheManager.getCache(CACHE_NAME);
                if (cache != null) {
                    cache.put(updatedRole.getRoleId(), updatedRole);
                }
            }
        }

        return result;
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "#roleId", unless = "#result == null")
    public SysRole selectRoleById(Integer roleId) {
        return roleMapper.selectRoleById(roleId);
    }
}
