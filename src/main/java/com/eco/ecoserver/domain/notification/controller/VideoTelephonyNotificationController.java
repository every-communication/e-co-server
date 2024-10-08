package com.eco.ecoserver.domain.notification.controller;


import com.eco.ecoserver.domain.notification.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/notifications/video-telephony")
public class VideoTelephonyNotificationController {
    private final NotificationService notificationService;
    @Autowired // Make sure you have this annotation
    public VideoTelephonyNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    @PatchMapping("/read/{notificationId}")
    public ResponseEntity<String> markAsRead(HttpServletRequest request, @PathVariable Long notificationId) throws IOException {
        boolean success = notificationService.markAsRead(request, "video-telephony", notificationId);
        if (success) {
            return ResponseEntity.ok("Notification marked as read.");
        } else {
            return ResponseEntity.status(404).body("Notification not found.");
        }
    }
}
