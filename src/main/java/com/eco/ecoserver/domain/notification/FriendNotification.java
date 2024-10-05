package com.eco.ecoserver.domain.notification;

import com.eco.ecoserver.domain.user.service.UserService;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@Entity
@AllArgsConstructor
public class FriendNotification extends Notification {
    @Column(nullable = false)
    private Long friendRequestListId;

    public FriendNotification() {
        super();
        setNotificationType(NotificationType.FRIEND);
    }
}
