package com.eco.ecoserver.domain.notification.service;

import com.eco.ecoserver.domain.notification.FriendNotification;
import com.eco.ecoserver.domain.notification.repository.FriendNotificationRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final FriendNotificationRepository friendNotificationRepository;

    public NotificationService(FriendNotificationRepository friendNotificationRepository) {
        this.friendNotificationRepository = friendNotificationRepository;
    }

    // 사용자의 알림 목록 가져오기
    public List<FriendNotification> getNotificationsByUserId(Long userId) {
        logger.info("Fetching notifications for user ID: {}", userId);
        return friendNotificationRepository.findByReceiptUserId(userId);
    }

    // 안 읽은 알림 개수 세기
    public long countUnreadNotifications(Long userId) {
        return friendNotificationRepository.countByReceiptUserIdAndViewFalse(userId);
    }

    // NotificationService에서 알림 읽음 처리 로직 추가
    public boolean markAsRead(Long notificationId) {
        Optional<FriendNotification> friendNotification = friendNotificationRepository.findById(notificationId);
        if (friendNotification.isPresent()) {
            FriendNotification notification = friendNotification.get();
            notification.setView(true); // 읽음 처리
            friendNotificationRepository.save(notification); // 업데이트된 상태 저장
            return true;
        } else {
            return false; // 알림이 없으면 false 반환
        }
    }

}
