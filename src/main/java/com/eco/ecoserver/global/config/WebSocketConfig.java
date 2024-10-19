package com.eco.ecoserver.global.config;


import com.eco.ecoserver.domain.videotelephony.handler.SignalingHandler;
import com.eco.ecoserver.global.interceptor.WebSocketAuthInterceptor;
import com.eco.ecoserver.global.jwt.service.JwtService;
import org.springframework.context.annotation.Configuration;


import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

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
                .addInterceptors(new WebSocketAuthInterceptor(jwtService))
                .setAllowedOrigins("*");
    }
}
