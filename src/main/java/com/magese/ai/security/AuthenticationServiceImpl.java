package com.magese.ai.security;

import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 密码加密与验证
 *
 * @author Joey
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final String salt = "joey@zhou";

    /**
     * 密码加密
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public String encryptPassword(String rawPassword) {
        String saltPassword = rawPassword + salt;
        StringBuilder result = new StringBuilder();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] bytes = messageDigest.digest(saltPassword.getBytes(StandardCharsets.UTF_8));
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1)
                    result.append("0");
                result.append(hex);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such algorithm MD5");
        }
        return result.toString();
    }

    /**
     * 密码验证
     *
     * @param rawPassword     原始密码
     * @param encryptPassword 加密密码
     * @return 是否相同
     */
    public Boolean isPasswordValid(String rawPassword, String encryptPassword) {
        String encodePassword = encryptPassword(rawPassword);
        return encodePassword.equals(encryptPassword);
    }

}
