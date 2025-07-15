package com.magese.ai.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magese.ai.common.web.AjaxResult;
import com.magese.ai.common.web.HttpStatus;
import com.magese.ai.entity.SysUser;
import com.magese.ai.service.SysUserService;
import com.magese.ai.utils.CmsUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final SysUserService userService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 不需要认证的路径
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/user/",
            "/api/device/ota",
            "/audio/",
            "/uploads/",
            "/ws/");

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        String path = request.getRequestURI();

        // 检查是否是公共路径
        if (isPublicPath(path)) {
            return true;
        }

        // 检查是否有@@UnLogin注解
        if (hasUnLoginAnnotation(handler)) {
            log.debug("接口 {} 标记为不需要登录验证", path);
            return true;
        }

        // 获取会话
        HttpSession session = request.getSession(false);
        if (session != null) {
            // 检查会话中是否有用户
            Object userObj = session.getAttribute(SysUserService.USER_SESSIONKEY);
            if (userObj != null) {
                SysUser user = (SysUser) userObj;
                // 将用户信息存储在请求属性中
                request.setAttribute(CmsUtils.USER_ATTRIBUTE_KEY, user);
                CmsUtils.setUser(request, user);
                return true;
            }
        }

        // 尝试从Cookie中获取用户名
        if (tryAuthenticateWithCookies(request)) {
            return true;
        }

        // 处理未授权的请求
        handleUnauthorized(request, response);
        return false;
    }

    /**
     * 尝试使用Cookie进行认证
     */
    private boolean tryAuthenticateWithCookies(HttpServletRequest request) {
        // 检查是否有username cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("username".equals(cookie.getName())) {
                    String username = cookie.getValue();
                    if (StringUtils.isNotBlank(username)) {
                        SysUser user = userService.selectUserByUsername(username);
                        if (user != null) {
                            // 将用户存储在会话和请求属性中
                            HttpSession session = request.getSession(true);
                            session.setAttribute(SysUserService.USER_SESSIONKEY, user);
                            request.setAttribute(CmsUtils.USER_ATTRIBUTE_KEY, user);
                            CmsUtils.setUser(request, user);
                            return true;
                        }
                    }
                    break;
                }
            }
        }
        return false;
    }

    /**
     * 处理未授权的请求
     */
    private void handleUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 检查是否是AJAX请求
        String ajaxTag = request.getHeader("Request-By");
        String head = request.getHeader("X-Requested-With");

        if ((ajaxTag != null && ajaxTag.trim().equalsIgnoreCase("Ext"))
                || (head != null && !head.equalsIgnoreCase("XMLHttpRequest"))) {
            response.addHeader("_timeout", "true");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            // 返回JSON格式的错误信息
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            AjaxResult result = AjaxResult.error(HttpStatus.FORBIDDEN, "用户未登录");
            try {
                objectMapper.writeValue(response.getOutputStream(), result);
            } catch (Exception e) {
                log.error("写入响应失败", e);
                throw e;
            }
        }
    }

    /**
     * 检查是否是公共路径
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * 检查处理器是否有@UnLogin注解
     */
    private boolean hasUnLoginAnnotation(Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return false;
        }

        Method method = handlerMethod.getMethod();
        Class<?> controllerClass = handlerMethod.getBeanType();

        // 检查方法上是否有@UnLogin注解
        UnLogin methodAnnotation = method.getAnnotation(UnLogin.class);
        if (methodAnnotation != null && methodAnnotation.value()) {
            return true;
        }

        // 检查类上是否有@UnLogin注解
        UnLogin classAnnotation = controllerClass.getAnnotation(UnLogin.class);
        return classAnnotation != null && classAnnotation.value();
    }
}
