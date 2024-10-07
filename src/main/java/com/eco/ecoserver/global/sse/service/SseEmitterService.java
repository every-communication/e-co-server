package com.eco.ecoserver.global.sse.service;

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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SseEmitterService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledExecutorService> executors = new ConcurrentHashMap<>(); // executor 저장 맵

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserService userService;
    @Autowired
    private NotificationService notificationService;


    public SseEmitter subscribe(HttpServletRequest request) throws IOException {
        // JWT 토큰에서 이메일 추출
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
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // 1 min timeout
        emitters.put(userId, emitter); // 사용자 ID를 키로 사용
        log.info("SseEmitter created for userId: {}", userId);

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            executors.remove(userId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            executors.remove(userId);
        });
        emitter.onError(e -> {
            emitters.remove(userId);
            executors.remove(userId);
        });

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executors.put(userId, executor);
        executor.scheduleAtFixedRate(() -> {
            try {
                long unreadCount = notificationService.countUnreadNotifications(userId);
                emitter.send(SseEmitter.event()
                        .name("unread-count")
                        .data(unreadCount));
            } catch (IOException e) {
                emitters.remove(userId);
                executors.remove(userId);
                log.error("Failed to send ping to userId: {}", userId, e);
                executor.shutdown(); // 오류 발생 시 executor 종료
            }
        }, 0, 5, TimeUnit.SECONDS); // 30초마다 ping 전송

        return emitter;
    }


    // 알림 전송 메서드
    public SseEmitter sendNotification(HttpServletRequest request, String notification) throws IOException {
        Optional<String> emailOpt = jwtService.extractEmailFromToken(request);
        if(emailOpt.isEmpty()) {
            throw new IOException("이메일 없음");
        }
        Optional<User> userOpt = userService.findByEmail(emailOpt.get());
        if(userOpt.isEmpty()) {
            throw new IOException("유저 없음");
        }

        Long userId = userOpt.get().getId();
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            long unreadCount = notificationService.countUnreadNotifications(userId);

            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
                emitter.send(SseEmitter.event()
                        .name("unread-count")
                        .data(unreadCount));
                log.info("Notification sent to userId: {}", userId);
            } catch (IOException e) {
                emitters.remove(userId);
                log.error("Failed to send notification to userId: {}", userId, e);
            }
        }
        return emitter;
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
            emitters.remove(userId);
            ScheduledExecutorService executor = executors.get(userId);
            if (executor != null) {
                executor.shutdown(); // 실행 중인 executor 종료
            }
            log.info("Subscription cancelled for userId: " + userId);
        } else {
            log.info("No active subscription found for userId: " + userId);
        }
    }
}
