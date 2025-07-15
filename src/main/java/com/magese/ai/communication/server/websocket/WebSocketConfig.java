package com.magese.ai.communication.server.websocket;

import com.magese.ai.utils.CmsUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Slf4j
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    // 定义为public static以便其他类可以访问
    public static final String WS_PATH = "/ws/mg/ai/";

    @Resource
    private WebSocketHandler webSocketHandler;

    @Resource
    private CmsUtils cmsUtils;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, WS_PATH)
                .setAllowedOrigins("*");

        log.info("📡 WebSocket服务地址: {}", cmsUtils.getWebsocketAddress());
        log.info("==========================================================");
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(1024 * 1024); // 1MB
        container.setMaxSessionIdleTimeout(60000L); // 60 seconds
        container.setAsyncSendTimeout(5000L); // 5 seconds
        return container;
    }
}
