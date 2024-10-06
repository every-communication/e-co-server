package com.eco.ecoserver.domain.notification;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Builder
@Entity
@AllArgsConstructor
public class FriendRequestNotification extends Notification {
    @Column(nullable = false)
    private Long friendRequestListId;

    public FriendRequestNotification() {
        super();
        setNotificationType(NotificationType.FRIEND);
    }
}
