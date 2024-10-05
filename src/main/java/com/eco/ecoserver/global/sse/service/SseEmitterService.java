package com.eco.ecoserver.global.sse.service;

import com.eco.ecoserver.domain.notification.FriendNotification;
import com.eco.ecoserver.domain.notification.service.NotificationService;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SseEmitterService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserService userService;
    private NotificationService notificationService;

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


        SseEmitter emitter = new SseEmitter(60000L); // 300 seconds timeout
        emitters.put(userId, emitter); // 사용자 ID를 키로 사용
        log.info("SseEmitter created for userId: {}", userId);

        startSendingNotifications(userId, emitter);

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            stopSendingNotifications(userId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            stopSendingNotifications(userId);
        });
        emitter.onError(e -> {
            emitters.remove(userId);
            stopSendingNotifications(userId);
        });

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
                updateUnreadNotificationCount(userId); // 읽지 않은 알림 개수 업데이트
            } catch (IOException e) {
                emitters.remove(userId); // 오류 시 emitter 제거
            }
        }
    }

    private void updateUnreadNotificationCount(Long userId) {
        // 읽지 않은 알림 개수 업데이트 로직
        long unreadCount = notificationService.countUnreadNotifications(userId); // 사용자에 대한 읽지 않은 알림 개수 조회
        // 클라이언트에게 unreadCount 전송
        // 예를 들어, 추가적인 SSE 이벤트를 통해 전송할 수 있음
    }

    // 사용자에게 알림을 주기적으로 보내는 메서드
    private void startSendingNotifications(Long userId, SseEmitter emitter) {
        scheduler.scheduleAtFixedRate(() -> {
            // 특정 조건에 따라 알림 생성 로직
            //FriendNotification notification = createNotificationForUser(userId); // 사용자를 위한 알림 생성
            //if (notification != null) {
            //    sendNotification(userId, notification); // 알림 전송
            //}
        }, 0, 5, TimeUnit.SECONDS); // 5초마다 알림 전송
    }

    // 구독 취소 시 알림 전송을 중지하는 메서드
    private void stopSendingNotifications(Long userId) {
        // 특정 사용자에 대한 알림 전송 중지 로직 (예: 스케줄러 종료)
        // 추가적인 작업이 필요할 수 있음
        log.info("Stopped sending notifications for userId: {}", userId);
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
