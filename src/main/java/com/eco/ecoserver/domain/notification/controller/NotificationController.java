package com.eco.ecoserver.domain.notification.controller;

import com.eco.ecoserver.domain.notification.FriendNotification;
import com.eco.ecoserver.domain.notification.dto.FriendNotificationDto;
import com.eco.ecoserver.domain.notification.service.SseEmitterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final SseEmitterService sseEmitterService;

    public NotificationController(SseEmitterService sseEmitterService) {
        this.sseEmitterService = sseEmitterService;
    }

    @GetMapping("/subscribe/{userId}")
    public SseEmitter subscribe(@PathVariable Long userId) {
        return sseEmitterService.createEmitter(userId);
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody FriendNotificationDto notificationDto) {
        // Process the notification data and store it in the database
        FriendNotification notification = new FriendNotification();
        notification.setTupleId(notificationDto.getTupleId());
        notification.setTitle(notificationDto.getTitle());
        notification.setMessage(notificationDto.getMessage());
        notification.setView(false);
        notification.setFriendRequestListId(notificationDto.getFriendRequestListId());
        notification.setRequestUserId(notificationDto.getRequestUserId());
        notification.setReceiptUserId(notificationDto.getReceiptUserId());

        // Save notification to database and notify the recipient
        sseEmitterService.sendNotification(notificationDto.getRequestUserId(), notification);

        return ResponseEntity.ok("Notification sent successfully.");
    }
}
