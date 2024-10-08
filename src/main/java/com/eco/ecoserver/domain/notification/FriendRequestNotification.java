package com.eco.ecoserver.domain.notification;

import com.eco.ecoserver.domain.notification.dto.NotificationDto;
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
        setNotificationType(NotificationType.FRIEND_REQUEST);
    }

    public FriendRequestNotification(Long friendRequestListId, Long requestUserId, Long receiptUserId) {
        setRequestUserId(requestUserId);
        setReceiptUserId(receiptUserId);
        setFriendRequestListId(friendRequestListId);
        setTitle("friend request notification-title");
        setMessage("friend request notification-message");
        setNotificationType(NotificationType.FRIEND_REQUEST);
        prePersist();
    }
}
