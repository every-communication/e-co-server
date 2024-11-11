package com.eco.ecoserver.domain.notification;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum NotificationType {
    FRIEND_REQUEST,
    VIDEO_TELEPHONY
}
