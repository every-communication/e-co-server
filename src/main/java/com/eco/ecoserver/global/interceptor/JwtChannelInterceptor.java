package com.eco.ecoserver.global.interceptor;


import com.eco.ecoserver.global.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtChannelInterceptor implements ChannelInterceptor {

    public static final String AUTHORIZATION = "Authorization";
    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // STOMP CONNECT 커맨드인지 확인
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Authorization 헤더에서 JWT 토큰을 가져옵니다.
            Optional<String> jwtTokenOptional = Optional.ofNullable(accessor.getFirstNativeHeader(AUTHORIZATION));

            // 토큰이 존재하는지 확인
            if (jwtTokenOptional.isPresent()) {
                String accessToken = jwtTokenOptional.get();

                // JWT 유효성 검사
                if (!jwtService.isTokenValid(accessToken)) {
                    log.error("유효하지 않은 JWT 토큰입니다.");
                    // 유효하지 않은 토큰의 경우 연결 거부 처리
                    throw new RuntimeException("Invalid JWT Token");
                }

                // JWT에서 이메일 추출
                String email = jwtService.extractEmail(accessToken).orElse(null);
                if (email == null) {
                    log.error("JWT에서 이메일을 추출할 수 없습니다.");
                    throw new RuntimeException("Could not extract email from JWT");
                }

                // Authentication 객체를 생성하고 설정
                Authentication authentication = createAuthentication(email);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                accessor.setUser(authentication);
                log.info("User authenticated: {}", email);
            } else {
                log.error("JWT 토큰이 없습니다.");
                throw new RuntimeException("JWT Token is missing");
            }
        }

        return message; // 다음 인터셉터로 메시지를 전달
    }

    private Authentication createAuthentication(String userId) {
        // Authentication 객체 생성 (여기서는 간단한 방식으로 사용)
        return new UsernamePasswordAuthenticationToken(userId, null);
    }
}
