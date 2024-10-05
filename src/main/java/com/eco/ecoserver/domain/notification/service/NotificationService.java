package com.eco.ecoserver.domain.notification.service;

import com.eco.ecoserver.domain.notification.FriendNotification;
import com.eco.ecoserver.domain.notification.NotificationType;
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

    public void createNotificationForUser(Long userId, String title, String message, Long friendRequestListId, Long requestUserId, Long receiptUserId) {
        FriendNotification notification = new FriendNotification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setFriendRequestListId(friendRequestListId);
        notification.setRequestUserId(requestUserId);
        notification.setReceiptUserId(receiptUserId);
        notification.setView(false); // 새로운 알림은 기본적으로 읽지 않음
        // createdAt은 기본 생성자에서 현재 시각으로 설정됩니다.

        notificationRepository.save(notification);
    }

    public void createNotificationForUser(Long userId, String title, String message, Long friendRequestListId, Long requestUserId, Long receiptUserId) {
        FriendNotification notification = new FriendNotification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setFriendRequestListId(friendRequestListId);
        notification.setRequestUserId(requestUserId);
        notification.setReceiptUserId(receiptUserId);
        notification.setView(false); // 새로운 알림은 기본적으로 읽지 않음
        // createdAt은 기본 생성자에서 현재 시각으로 설정됩니다.

        notificationRepository.save(notification);
    }

    public void generateMessage(NotificationType notificationType, Long notificationId, Long requestUserId) {

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
