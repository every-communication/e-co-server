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
    private Long videoTelephonyRoomId; // 화상 통화 방 ID

    // 기본 생성자
    public VideoTelephonyNotification() {
        super(); // 부모 클래스의 생성자 호출
        setNotificationType(NotificationType.VIDEO_TELEPHONY);
    }
}
