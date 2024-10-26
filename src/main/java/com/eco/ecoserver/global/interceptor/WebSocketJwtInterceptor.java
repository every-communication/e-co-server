package com.eco.ecoserver.global.interceptor;

import com.eco.ecoserver.global.jwt.service.JwtService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class WebSocketJwtInterceptor implements HandshakeInterceptor {
    private final JwtService jwtService;

    public WebSocketJwtInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 개발을 위해 임시로 모든 연결 허용
        return true;

        // 또는 토큰이 있을 때만 검증하고, 없으면 통과하도록
            /*
            String query = request.getURI().getQuery();
            if (query != null && query.contains("token=")) {
                String token = extractToken(query);
                Optional<String> email = jwtService.extractEmail(token);
                if (email.isPresent()) {
                    attributes.put("email", email.get());
                    return true;
                }
                return false;
            }
            return true;  // 토큰이 없으면 통과
            */
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}