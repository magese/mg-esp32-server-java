package com.magese.ai.service;

import com.magese.ai.common.web.PageFilter;
import com.magese.ai.entity.SysDevice;
import org.apache.ibatis.javassist.NotFoundException;

import java.util.List;

/**
 * 设备查询/更新
 *
 * @author Joey
 */
public interface SysDeviceService {

    /**
     * 添加设备
     */
    int add(SysDevice device) throws NotFoundException;

    /**
     * 查询设备信息
     */
    List<SysDevice> query(SysDevice device, PageFilter pageFilter);

    /**
     * 查询设备信息，并join配置表联查，用来过滤不存在的configId
     */
    SysDevice selectDeviceById(String deviceId);

    /**
     * 查询验证码
     */
    SysDevice queryVerifyCode(SysDevice device);

    /**
     * 查询并生成验证码
     */
    SysDevice generateCode(SysDevice device);

    /**
     * 关系设备验证码语音路径
     */
    int updateCode(SysDevice device);

    /**
     * 更新设备信息
     */
    int update(SysDevice device);

    /**
     * 删除设备
     */
    int delete(SysDevice device);

    /**
     * 生成设备访问平台的token
     */
    String generateToken(String deviceId);
}
