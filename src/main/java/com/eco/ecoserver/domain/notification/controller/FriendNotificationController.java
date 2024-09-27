package com.eco.ecoserver.domain.notification.controller;

import com.eco.ecoserver.domain.notification.FriendNotification;
import com.eco.ecoserver.domain.notification.service.NotificationService;
import com.eco.ecoserver.domain.notification.service.SseEmitterService;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.global.jwt.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/notifications/friend")
public class FriendNotificationController {
    private final SseEmitterService sseEmitterService;
    private final UserService userService;
    private final NotificationService notificationService; // 알림 서비스 주입
    JwtService jwtService;

    public FriendNotificationController(SseEmitterService sseEmitterService, UserService userService, NotificationService notificationService) {
        this.sseEmitterService = sseEmitterService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    // 0. SSE 구독
    @GetMapping("/subscribe/{email}")
    public SseEmitter subscribe(@PathVariable String email, HttpServletRequest request) throws AccessDeniedException {
        return sseEmitterService.createEmitter(request);
    }

    // 1. 알림 목록 불러오기 API
    @GetMapping("/list/{email}")
    public ResponseEntity<List<FriendNotification>> getNotificationList(@PathVariable String email) {
        Optional<User> optionalUser = userService.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            List<FriendNotification> notifications = notificationService.getNotificationsByUserId(user.getId());
            return ResponseEntity.ok(notifications);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.emptyList()); // 사용자 미존재 시 빈 리스트 반환
        }
    }

    // 2. 미확인 알림 개수
    @GetMapping("/unread-count/{email}")
    public ResponseEntity<Long> getUnreadNotificationCount(@PathVariable String email) {
        Optional<User> optionalUser = userService.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            long unreadCount = notificationService.countUnreadNotifications(user.getId());
            return ResponseEntity.ok(unreadCount);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(0L); // 사용자 미존재 시 0 반환
        }
    }

    // NotificationController에서 알림 읽음 처리 메서드 추가
    @PatchMapping("/read/{notificationId}")
    public ResponseEntity<String> markAsRead(@PathVariable Long notificationId) {
        boolean success = notificationService.markAsRead(notificationId);
        if (success) {
            return ResponseEntity.ok("Notification marked as read.");
        } else {
            return ResponseEntity.status(404).body("Notification not found.");
        }
    }

    // SSE 구독 취소
    @PostMapping("/unsubscribe/{email}")
    public ResponseEntity<String> unsubscribe(@PathVariable String email, HttpServletRequest request) throws AccessDeniedException {
        sseEmitterService.cancelSubscription(request);
        return ResponseEntity.ok("구독이 취소되었습니다.");
    }
}
