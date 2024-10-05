package com.eco.ecoserver.domain.notification.controller;

import com.eco.ecoserver.domain.notification.FriendNotification;
import com.eco.ecoserver.domain.notification.service.NotificationService;
import com.eco.ecoserver.global.sse.service.SseEmitterService;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.global.jwt.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.swing.text.html.Option;
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
    private final JwtService jwtService;

    public FriendNotificationController(SseEmitterService sseEmitterService, UserService userService, NotificationService notificationService, JwtService jwtService) {
        this.sseEmitterService = sseEmitterService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.jwtService = jwtService;
    }

    // 1. 친구 알림 목록 불러오기 API
    @GetMapping("/list")
    public ResponseEntity<List<FriendNotification>> getNotificationList(HttpServletRequest request) {
        Optional<String> emailOpt = jwtService.extractEmailFromToken(request);

        if(emailOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<User> userOpt = userService.findByEmail(emailOpt.get());
        if(userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userOpt.get();
        List<FriendNotification> notifications = notificationService.getNotificationsByUserId(user.getId());
        return ResponseEntity.ok(notifications);
    }

    // 2. 미확인 알림 개수
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadNotificationCount(HttpServletRequest request) {
        Optional<String> emailOpt = jwtService.extractEmailFromToken(request);
        if(emailOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> userOpt = userService.findByEmail(emailOpt.get());
        if(userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userOpt.get();
        long unreadCount = notificationService.countUnreadNotifications(user.getId());
        return ResponseEntity.ok(unreadCount);
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

}
