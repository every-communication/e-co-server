package com.eco.ecoserver.domain.notification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationDto {
    private Long tupleId;
    private String title;
    private String message;
    private Long friendRequestListId;
    private Long requestUserId;
    private Long receiptUserId;
}
