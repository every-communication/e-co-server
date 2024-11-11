package com.eco.ecoserver.domain.notification;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@Entity
@AllArgsConstructor
public class VideoTelephonyNotification extends Notification {
    @Column(nullable = false)
    private Long roomId; // 화상 통화 방 ID

    // 기본 생성자
    public VideoTelephonyNotification() {
        super(); // 부모 클래스의 생성자 호출
        setNotificationType(NotificationType.VIDEO_TELEPHONY);
    }

    public VideoTelephonyNotification(Long roomId, Long requestUserId, Long receiptUserId) {
        setRequestUserId(requestUserId);
        setReceiptUserId(receiptUserId);
        setRoomId(roomId);
        setTitle("video telephony notification-title");
        setMessage("video telephony notification-message");
        setNotificationType(NotificationType.VIDEO_TELEPHONY);
        prePersist();
    }
}
