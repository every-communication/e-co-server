package com.eco.ecoserver.domain.notification.repository;

import com.eco.ecoserver.domain.notification.FriendNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendNotificationRepository extends JpaRepository<FriendNotification, Long> {
    List<FriendNotification> findByRequestUserId(Long userId);
    List<FriendNotification> findByReceiptUserId(Long userId);
    long countByReceiptUserIdAndViewFalse(Long userId);
}