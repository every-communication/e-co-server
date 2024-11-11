package com.eco.ecoserver.domain.notification.dto;

import com.eco.ecoserver.domain.notification.NotificationType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Data
public class VideoNotificationDto {
    private String title;
    private String message;

    private String roomCode;
    private Long notificationId;
    private Long requestUserId;
    private String requestUserEmail;

    private LocalDateTime timestamp;

    @JsonProperty
    private NotificationType notificationType;
}
