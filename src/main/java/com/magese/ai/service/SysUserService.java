package com.magese.ai.service;

import com.magese.ai.common.exception.UserPasswordNotMatchException;
import com.magese.ai.common.exception.UsernameNotFoundException;
import com.magese.ai.common.web.PageFilter;
import com.magese.ai.entity.SysUser;

import java.util.List;

/**
 * 用户操作
 *
 * @author Joey
 */
public interface SysUserService {

    /**
     * 用户名sessionkey
     */
    String USER_SESSIONKEY = "user_sessionkey";

    /**
     * 登录校验
     */
    SysUser login(String username, String password)
            throws UsernameNotFoundException, UserPasswordNotMatchException;

    /**
     * 查询用户信息
     *
     * @return 用户信息
     */
    SysUser query(String username);

    /**
     * 用户查询列表
     *
     * @return 用户列表
     */
    List<SysUser> queryUsers(SysUser user, PageFilter pageFilter);

    SysUser selectUserByUserId(Integer userId);

    SysUser selectUserByUsername(String username);

    SysUser selectUserByEmail(String email);

    /**
     * 新增用户
     */
    int add(SysUser user);

    /**
     * 修改用户信息
     */
    int update(SysUser user);

    /**
     * 生成验证码
     */
    SysUser generateCode(SysUser user);

    /**
     * 查询验证码是否有效
     */
    int queryCaptcha(String code, String email);

}
