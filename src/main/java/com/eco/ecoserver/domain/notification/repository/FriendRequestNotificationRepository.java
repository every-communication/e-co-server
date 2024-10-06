package com.eco.ecoserver.domain.notification.repository;

import com.eco.ecoserver.domain.notification.FriendRequestNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRequestNotificationRepository extends JpaRepository<FriendRequestNotification, Long> {
    List<FriendRequestNotification> findByRequestUserId(Long userId);
    List<FriendRequestNotification> findByReceiptUserId(Long userId);
    long countByReceiptUserIdAndViewFalse(Long userId);
}