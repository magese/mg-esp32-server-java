package com.magese.ai.common.exception;

import com.magese.ai.common.web.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 *
 * @author Joey
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 用户名不存在异常
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AjaxResult handleUsernameNotFoundException(UsernameNotFoundException e) {
        log.warn("用户名不存在异常: {}", e.getMessage(), e);
        return AjaxResult.error("用户名不存在");
    }

    /**
     * 用户密码不匹配异常
     */
    @ExceptionHandler(UserPasswordNotMatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AjaxResult handleUserPasswordNotMatchException(UserPasswordNotMatchException e) {
        log.warn("用户密码不匹配异常: {}", e.getMessage(), e);
        return AjaxResult.error("用户密码不正确");
    }

    /**
     * 静态资源找不到异常
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public AjaxResult handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("静态资源找不到: {}", e.getResourcePath());
        return AjaxResult.error(HttpStatus.NOT_FOUND.value(), "请求的资源不存在");
    }

    /**
     * 业务异常处理
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AjaxResult handleRuntimeException(RuntimeException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return AjaxResult.error("操作失败：" + e.getMessage());
    }

    /**
     * 系统异常 - 作为最后的兜底处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public AjaxResult handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return AjaxResult.error("服务器错误，请联系管理员");
    }
}
