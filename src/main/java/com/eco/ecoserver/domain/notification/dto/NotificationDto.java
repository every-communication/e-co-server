package com.eco.ecoserver.domain.notification.dto;

import com.eco.ecoserver.domain.notification.NotificationType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationDto {

    private String title;
    private String message;
    private boolean view;

    private Long notificationId;
    private Long requestUserId;
    private Long receiptUserId;

    private LocalDateTime timestamp;
    private NotificationType notificationType;
}
