package com.eco.ecoserver.global.config;


import com.eco.ecoserver.domain.videotelephony.handler.SignalingHandler;
import com.eco.ecoserver.global.interceptor.WebSocketAuthInterceptor;
import com.eco.ecoserver.global.interceptor.WebSocketJwtInterceptor;
import com.eco.ecoserver.global.jwt.service.JwtService;
import org.springframework.context.annotation.Configuration;


import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SignalingHandler signalingHandler;
    private final JwtService jwtService;

    public WebSocketConfig(SignalingHandler signalingHandler, JwtService jwtService) {
        this.signalingHandler = signalingHandler;
        this.jwtService = jwtService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(signalingHandler, "/signal")
                .setAllowedOrigins("*")  // 또는 특정 origin만 허용
                .addInterceptors(new WebSocketJwtInterceptor(jwtService)); // 인터셉터 제거하거나 모든 연결 허용하도록 수정
    }


}
