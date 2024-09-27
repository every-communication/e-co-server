package com.eco.ecoserver.domain.notification.service;

import com.eco.ecoserver.domain.notification.FriendNotification;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.global.jwt.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseEmitterService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserService userService;

    public SseEmitter createEmitter(HttpServletRequest request) throws AccessDeniedException {
        // JWT 토큰에서 이메일 추출
        Optional<String> emailOpt = jwtService.extractEmailFromToken(request);

        if (emailOpt.isEmpty()) {
            log.info("No email found");
            throw new AccessDeniedException("권한이 없습니다."); // 권한 거부 처리
        }

        String email = emailOpt.get();
        log.info("Extracted email: {}", email);

        // 이메일로 사용자 찾기
        Optional<User> userOpt = userService.findByEmail(email);
        log.info("User found: {}", userOpt.get().getId());
        if (userOpt.isEmpty()) {
            log.info("No user found");
            throw new AccessDeniedException("사용자를 찾을 수 없습니다."); // 사용자 찾기 실패
        }

        Long userId = userOpt.get().getId(); // 사용자 ID 추출


        SseEmitter emitter = new SseEmitter(60000L); // 60 seconds timeout
        emitters.put(userId, emitter); // 사용자 ID를 키로 사용
        log.info("SseEmitter created for userId: {}", userId);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        return emitter;
    }

    public void sendNotification(Long userId, FriendNotification notification) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification)
                        .reconnectTime(5000));
            } catch (IOException e) {
                emitters.remove(userId); // Remove emitter on error
            }
        }
    }

    public void cancelSubscription(HttpServletRequest request) throws AccessDeniedException {
        // JWT 토큰에서 이메일 추출
        Optional<String> emailOpt = jwtService.extractEmailFromToken(request);

        if (emailOpt.isEmpty()) {
            log.info("No email found");
            throw new AccessDeniedException("권한이 없습니다."); // 권한 거부 처리
        }

        String email = emailOpt.get();

        // 이메일로 사용자 찾기
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.info("No user found");
            throw new AccessDeniedException("사용자를 찾을 수 없습니다."); // 사용자 찾기 실패
        }

        Long userId = userOpt.get().getId(); // 사용자 ID 추출

        // 구독 취소: emitters 맵에서 제거
        if (emitters.containsKey(userId)) {
            emitters.remove(userId);
            log.info("Subscription cancelled for userId: " + userId);
        } else {
            log.info("No active subscription found for userId: " + userId);
        }
    }
}
