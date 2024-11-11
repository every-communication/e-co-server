package com.eco.ecoserver.global.sse.service;

import com.eco.ecoserver.domain.notification.Notification;
import com.eco.ecoserver.domain.notification.NotificationType;
import com.eco.ecoserver.domain.notification.service.NotificationService;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.global.jwt.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    public SseEmitter subscribe(HttpServletRequest request) throws IOException {
        Optional<String> emailOpt = jwtService.extractEmailFromToken(request);

        if (emailOpt.isEmpty()) {
            log.info("No email found");
            throw new IOException("권한이 없습니다."); // 권한 거부 처리
        }

        String email = emailOpt.get();
        log.info("Extracted email: {}", email);

        // 이메일로 사용자 찾기
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.info("No user found");
            throw new IOException("사용자를 찾을 수 없습니다."); // 사용자 찾기 실패
        }
        log.info("User found: {}", userOpt.get().getId());

        Long userId = userOpt.get().getId(); // 사용자 ID 추출
        SseEmitter emitter = new SseEmitter(90 * 60 * 1000L); // 90 min
        emitters.put(userId, emitter); // 사용자 ID를 키로 사용
        log.info("SseEmitter created for userId: {}", userId);

//        emitter.onCompletion(() -> emitters.remove(userId));
//        emitter.onTimeout(() -> emitters.remove(userId));
//        emitter.onError(e -> emitters.remove(userId));
        // 자동 구독 해제를 위한 이벤트 핸들러 설정
        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.info("SseEmitter completed for userId: {}", userId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.info("SseEmitter timed out for userId: {}", userId);
        });
        emitter.onError(e -> {
            emitters.remove(userId);
            log.error("SseEmitter error for userId: {}", userId, e);
        });

        return emitter;
    }


    // 알림 전송 메서드
    public void sendNotification(Long userId, String eventName, String message) throws IOException {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(message));
                log.info("Notification sent to userId: {}", userId);
                log.info("Notification sent to event: {}", eventName);
            } catch (IOException e) {
                emitters.remove(userId);
                log.error("Failed to send notification to userId: {}", userId, e);
            }
        }
    }

    public void sendNotification(Long userId, String eventName, Object data) throws IOException {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
                log.info("Notification sent to userId: {}", userId);
                log.info("Notification sent to event: {}", eventName);
            } catch (IOException e) {
                emitters.remove(userId);
                log.error("Failed to send notification to userId: {}", userId, e);
            }
        }
    }

    public void cancelSubscription(HttpServletRequest request) throws IOException {
        // JWT 토큰에서 이메일 추출
        Optional<String> emailOpt = jwtService.extractEmailFromToken(request);
        if (emailOpt.isEmpty()) {
            log.info("No email found");
            throw new IOException("권한이 없습니다."); // 권한 거부 처리
        }

        String email = emailOpt.get();
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.info("No user found");
            throw new IOException("사용자를 찾을 수 없습니다."); // 사용자 찾기 실패
        }

        Long userId = userOpt.get().getId(); // 사용자 ID 추출
        if (emitters.containsKey(userId)) {
            SseEmitter emitter = emitters.get(userId);
            if (emitter != null) {
                emitter.complete(); // 클라이언트와의 연결을 명시적으로 종료
            }
            emitters.remove(userId);

            log.info("Subscription cancelled for userId: " + userId);
        } else {
            log.info("No active subscription found for userId: " + userId);
        }
    }
}
