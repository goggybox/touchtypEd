package com.example.touchTypEd_backend.config;

import com.example.touchTypEd_backend.websocket.TypingWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TypingWebSocketHandler typingHandler;

    public WebSocketConfig(TypingWebSocketHandler typingHandler) {
        this.typingHandler = typingHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(typingHandler, "/ws")
                .setAllowedOrigins("*"); // 允许所有origin访问, 也可指定前端地址
    }
}
