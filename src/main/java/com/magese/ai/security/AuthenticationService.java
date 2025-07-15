package com.magese.ai.security;

/**
 * 密码加密与验证
 *
 * @author Joey
 */

public interface AuthenticationService {
    /**
     * 密码加密
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    String encryptPassword(String rawPassword);

    /**
     * 密码验证
     *
     * @param rawPassword     原始密码
     * @param encryptPassword 加密密码
     * @return 是否相同
     */
    Boolean isPasswordValid(String rawPassword, String encryptPassword);
}
