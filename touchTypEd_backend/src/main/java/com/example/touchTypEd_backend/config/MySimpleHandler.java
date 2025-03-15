package com.example.touchTypEd_backend.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class MySimpleHandler extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("新客户端连接: " + session.getId());
        session.sendMessage(new TextMessage("Welcome! Connected to server."));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("收到消息: " + payload);
        // 简单回显(Echo)
        session.sendMessage(new TextMessage("Server echo: " + payload));
    }
}
