package com.magese.ai.common.config;

import com.magese.ai.common.interceptor.AuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.io.File;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/device/ota",
                        "/audio/**",
                        "/uploads/**",
                        "/ws/**",
                        // 添加 swagger 相关路径
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(@NotNull ResourceHandlerRegistry registry) {
        try {
            // 获取项目根目录的绝对路径
            String basePath = new File("").getAbsolutePath();

            // 音频文件存储在项目根目录下的audio文件夹中
            String audioPath = "file:" + basePath + File.separator + "audio" + File.separator;

            // 上传文件存储在项目根目录下的uploads文件夹中
            String uploadsPath = "file:" + basePath + File.separator + "uploads" + File.separator;

            // 配置资源映射
            registry.addResourceHandler("/audio/**")
                    .addResourceLocations(audioPath);

            // 为上传文件添加资源映射
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations(uploadsPath);

        } catch (Exception e) {
            log.error("添加资源失败", e);
        }
    }

    /**
     * 配置路径匹配参数
     */
    @Override
    @SuppressWarnings("deprecation") // 暂时抑制过时警告
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 使用推荐的方法设置尾部斜杠匹配
        configurer.setUseTrailingSlashMatch(true);
    }
}
