package com.eco.ecoserver.domain.notification.controller;

import com.eco.ecoserver.domain.notification.Notification;
import com.eco.ecoserver.domain.notification.NotificationType;
import com.eco.ecoserver.domain.notification.service.NotificationService;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.sse.service.SseEmitterService;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.global.jwt.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final SseEmitterService sseEmitterService;
    private final UserService userService;
    private final NotificationService notificationService; // 알림 서비스 주입
    private final JwtService jwtService;

    public NotificationController(SseEmitterService sseEmitterService, UserService userService, NotificationService notificationService, JwtService jwtService) {
        this.sseEmitterService = sseEmitterService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.jwtService = jwtService;
    }

    // 알림 목록 불러오기
    @GetMapping("/list")
    public ResponseEntity<ApiResponseDto<?>> getNotificationList(HttpServletRequest request) {
        Optional<String> emailOpt = jwtService.extractEmailFromToken(request);

        if(emailOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponseDto.failure("Unauthorizaed"));
        }
        Optional<User> userOpt = userService.findByEmail(emailOpt.get());
        if(userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponseDto.failure("Unauthorizaed"));
        }
        User user = userOpt.get();
        List<Notification> notifications = notificationService.getAllNotificationsByUserId(user.getId());
        return ResponseEntity.status(200).body(ApiResponseDto.success(notifications));
    }

    // 2. 미확인 알림 개수
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponseDto<?>> getUnreadNotificationCount(HttpServletRequest request) throws IOException {
        Optional<String> emailOpt = jwtService.extractEmailFromToken(request);
        if(emailOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponseDto.failure("Unauthorizaed"));
        }

        Optional<User> userOpt = userService.findByEmail(emailOpt.get());
        if(userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponseDto.failure("Unauthorizaed"));
        }
        User user = userOpt.get();
        long unreadCount = notificationService.countUnreadNotifications(user.getId());
        return ResponseEntity.status(200).body(ApiResponseDto.success(unreadCount));
    }

    @PatchMapping("/read/{notificationType}/{notificationId}")
    public ResponseEntity<String> markAsRead(HttpServletRequest request,
                                             @PathVariable String notificationType,
                                             @PathVariable Long notificationId) throws IOException {
        boolean success = notificationService.markAsRead(request, notificationType, notificationId);

        if (success) {
            return ResponseEntity.ok("Notification marked as read.");
        } else {
            return ResponseEntity.status(404).body("Notification not found.");
        }
    }
}
