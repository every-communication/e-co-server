package com.eco.ecoserver.domain.notification.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VideoTelephonyRequestDto {
    private Long notificationId;

    private Long roomId;
    private String roomCode;

    private Long requestUserId;
    private String requestUserName;
    private String requestUserEmail;
    private String requestUserThumbnail;

    private String requestTime;
}
