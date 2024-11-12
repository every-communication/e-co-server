package com.eco.ecoserver.domain.notification.controller;


import com.eco.ecoserver.domain.notification.VideoTelephonyNotification;
import com.eco.ecoserver.domain.notification.dto.NotificationDto;
import com.eco.ecoserver.domain.notification.dto.VideoNotificationDto;
import com.eco.ecoserver.domain.notification.dto.VideoTelephonyRequestDto;
import com.eco.ecoserver.domain.notification.repository.VideoTelephonyNotificationRepository;
import com.eco.ecoserver.domain.notification.service.NotificationService;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.jwt.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications/video-telephony")
public class VideoTelephonyNotificationController {
    private final JwtService jwtService;
    private final UserService userService;
    private final NotificationService notificationService;

    @PatchMapping("/read/{notificationId}")
    public ResponseEntity<String> markAsRead(HttpServletRequest request, @PathVariable Long notificationId) throws IOException {
        boolean success = notificationService.markAsRead(request, "video-telephony", notificationId);
        if (success) {
            return ResponseEntity.ok("Notification marked as read.");
        } else {
            return ResponseEntity.status(404).body("Notification not found.");
        }
    }

    @GetMapping("/requested")
    public ResponseEntity<ApiResponseDto<List<VideoTelephonyRequestDto>>> getRequestedNotifications(HttpServletRequest request) {
        Optional<String> email = jwtService.extractEmailFromToken(request);
        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorized"));
        }
        Optional<User> userOpt = userService.findByEmail(email.get());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorized"));
        }

        return ResponseEntity.ok().body(notificationService.getVideoNotifications(userOpt.get().getId()));
    }
}
