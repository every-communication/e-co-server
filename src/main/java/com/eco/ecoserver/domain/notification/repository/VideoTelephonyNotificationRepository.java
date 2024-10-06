package com.eco.ecoserver.domain.notification.repository;

import com.eco.ecoserver.domain.notification.VideoTelephonyNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoTelephonyNotificationRepository extends JpaRepository<VideoTelephonyNotification, Long> {
    List<VideoTelephonyNotification> findByReceiptUserId(Long receiptUserId);
    long countByReceiptUserIdAndViewFalse(Long receiptUserId);
}
