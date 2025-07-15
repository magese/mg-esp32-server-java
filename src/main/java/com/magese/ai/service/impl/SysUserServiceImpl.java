package com.magese.ai.service.impl;

import com.github.pagehelper.PageHelper;
import com.magese.ai.common.exception.UserPasswordNotMatchException;
import com.magese.ai.common.exception.UsernameNotFoundException;
import com.magese.ai.common.web.PageFilter;
import com.magese.ai.dao.UserMapper;
import com.magese.ai.entity.SysUser;
import com.magese.ai.security.AuthenticationService;
import com.magese.ai.service.SysUserService;
import com.magese.ai.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;


/**
 * 用户操作
 *
 * @author Joey
 */
@RequiredArgsConstructor
@Service
public class SysUserServiceImpl extends BaseServiceImpl implements SysUserService {

    private static final String dayOfMonthStart = DateUtils.dayOfMonthStart();
    private static final String dayOfMonthEnd = DateUtils.dayOfMonthEnd();

    private final UserMapper userMapper;
    private final AuthenticationService authenticationService;

    /**
     * 登录
     */
    @Override
    public SysUser login(String username, String password)
            throws UsernameNotFoundException, UserPasswordNotMatchException {
        SysUser user = userMapper.selectUserByUsername(username);
        if (ObjectUtils.isEmpty(user)) {
            throw new UsernameNotFoundException();
        } else if (!authenticationService.isPasswordValid(password, user.getPassword())) {
            throw new UserPasswordNotMatchException();
        }
        return user;
    }

    /**
     * 用户信息查询
     *
     * @return 用户信息
     */
    @Override
    public SysUser query(String username) {
        return userMapper.query(username, dayOfMonthStart, dayOfMonthEnd);
    }

    /**
     * 用户列表查询
     *
     * @return 用户列表
     */
    @Override
    public List<SysUser> queryUsers(SysUser user, PageFilter pageFilter) {
        if (pageFilter != null) {
            PageHelper.startPage(pageFilter.getStart(), pageFilter.getLimit());
        }
        return userMapper.queryUsers(user);
    }

    @Override
    public SysUser selectUserByUserId(Integer userId) {
        return userMapper.selectUserByUserId(userId);
    }

    @Override
    public SysUser selectUserByUsername(String username) {
        return userMapper.selectUserByUsername(username);
    }

    @Override
    public SysUser selectUserByEmail(String email) {
        return userMapper.selectUserByEmail(email);
    }

    /**
     * 新增用户
     */
    @Override
    @Transactional(transactionManager = "transactionManager")
    public int add(SysUser user) {
        return userMapper.add(user);
    }

    /**
     * 用户信息更改
     */
    @Override
    @Transactional(transactionManager = "transactionManager")
    public int update(SysUser user) {
        return userMapper.update(user);
    }

    /**
     * 生成验证码
     */
    @Override
    public SysUser generateCode(SysUser user) {
        SysUser result = new SysUser();
        userMapper.generateCode(user);
        result.setCode(user.getCode());
        return result;
    }

    /**
     * 查询验证码是否有效
     */
    @Override
    public int queryCaptcha(String code, String email) {
        return userMapper.queryCaptcha(code, email);
    }

}
