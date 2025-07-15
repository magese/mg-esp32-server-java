package com.magese.ai;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cache.annotation.EnableCaching;

@Slf4j
@SpringBootApplication
@EnableCaching
@MapperScan("com.magese.ai.dao")
public class MageseAIApplication {


    public static void main(String[] args) {
        try {
            SpringApplication.run(MageseAIApplication.class, args);
            ServerProperties serverProperties = SpringUtil.getBean(ServerProperties.class);
            Integer port = serverProperties.getPort();
            String contextPath = serverProperties.getServlet().getContextPath();
            log.info("MageseAIApplication 启动成功，端口：{}，上下文：{}", port, contextPath);
        } catch (Exception e) {
            log.error("MageseAIApplication 启动失败", e);
        }
    }
}
