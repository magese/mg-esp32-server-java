package com.magese.ai.service.impl;

import com.github.pagehelper.PageHelper;
import com.magese.ai.common.web.PageFilter;
import com.magese.ai.communication.common.ChatSession;
import com.magese.ai.communication.common.SessionManager;
import com.magese.ai.dao.DeviceMapper;
import com.magese.ai.dao.MessageMapper;
import com.magese.ai.dao.RoleMapper;
import com.magese.ai.entity.SysDevice;
import com.magese.ai.entity.SysMessage;
import com.magese.ai.entity.SysRole;
import com.magese.ai.service.SysDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 设备操作
 *
 * @author Joey
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysDeviceServiceImpl extends BaseServiceImpl implements SysDeviceService {

    private final static String CACHE_NAME = "Magese:SysDevice";

    private final DeviceMapper deviceMapper;
    private final MessageMapper messageMapper;
    private final RoleMapper roleMapper;
    private final SessionManager sessionManager;

    /**
     * 添加设备
     *
     * @throws NotFoundException 如果没有配置角色
     */
    @Override
    @Transactional(transactionManager = "transactionManager")
    public int add(SysDevice device) throws NotFoundException {

        SysDevice existingDevice = deviceMapper.selectDeviceById(device.getDeviceId());
        if (existingDevice != null) {
            return 1;
        }

        // 查询是否有默认角色
        SysRole queryRole = new SysRole();
        queryRole.setUserId(device.getUserId());
        List<SysRole> roles = roleMapper.query(queryRole);

        if (roles.isEmpty()) {
            throw new NotFoundException("没有配置角色");
        }

        SysRole selectedRole = null;

        // 优先绑定默认角色
        for (SysRole role : roles) {
            if (("1").equals(role.getIsDefault())) {
                selectedRole = role;
                break;
            }
        }
        if (selectedRole == null) {
            selectedRole = roles.getFirst();
        }

        device.setRoleId(selectedRole.getRoleId());
        return deviceMapper.add(device);

    }

    /**
     * 删除设备
     */
    @Override
    @Transactional(transactionManager = "transactionManager")
    @CacheEvict(value = CACHE_NAME, key = "#device.deviceId.replace(\":\", \"-\")")
    public int delete(SysDevice device) {
        int row = deviceMapper.delete(device);
        if (row > 0) {
            SysMessage message = new SysMessage();
            message.setUserId(device.getUserId());
            message.setDeviceId(device.getDeviceId());
            // 清空设备聊天记录
            messageMapper.delete(message);
        }
        return row;
    }

    /**
     * 查询设备信息
     */
    @Override
    public List<SysDevice> query(SysDevice device, PageFilter pageFilter) {
        if (pageFilter != null) {
            PageHelper.startPage(pageFilter.getStart(), pageFilter.getLimit());
        }
        return deviceMapper.query(device);
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "#deviceId.replace(\":\", \"-\")", unless = "#result == null")
    public SysDevice selectDeviceById(String deviceId) {
        return deviceMapper.selectDeviceById(deviceId);
    }

    /**
     * 查询验证码
     */
    @Override
    public SysDevice queryVerifyCode(SysDevice device) {
        return deviceMapper.queryVerifyCode(device);
    }

    /**
     * 查询并生成验证码
     */
    @Override
    public SysDevice generateCode(SysDevice device) {
        SysDevice result = deviceMapper.queryVerifyCode(device);
        if (result == null) {
            result = new SysDevice();
            deviceMapper.generateCode(device);
            result.setCode(device.getCode());
        }
        return result;
    }

    /**
     * 关系设备验证码语音路径
     */
    @Override
    public int updateCode(SysDevice device) {
        return deviceMapper.updateCode(device);
    }

    /**
     * 更新设备信息
     */
    @Override
    @Transactional(transactionManager = "transactionManager")
    @CacheEvict(value = CACHE_NAME, key = "#device.deviceId.replace(\":\", \"-\")")
    public int update(SysDevice device) {
        int rows = deviceMapper.update(device);
        // 更新设备信息后清空记忆缓存并重新注册设备信息
        device = deviceMapper.selectDeviceById(device.getDeviceId());
        ChatSession session = null;
        if (device != null) {
            session = sessionManager.getSessionByDeviceId(device.getDeviceId());
        }
        if (session != null) {
            session.setSysDevice(device);
        }
        return rows;
    }

    @Override
    public String generateToken(String deviceId) {
        String token = UUID.randomUUID().toString();
        deviceMapper.insertCode(deviceId, token);
        return token;
    }

}
