package com.eco.ecoserver.domain.notification.service;

import com.eco.ecoserver.domain.notification.FriendRequestNotification;
import com.eco.ecoserver.domain.notification.Notification;
import com.eco.ecoserver.domain.notification.VideoTelephonyNotification;
import com.eco.ecoserver.domain.notification.repository.FriendRequestNotificationRepository;
import com.eco.ecoserver.domain.notification.repository.VideoTelephonyNotificationRepository;
import com.eco.ecoserver.global.sse.service.SseEmitterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private FriendRequestNotificationRepository friendRequestNotificationRepository;
    @Autowired
    private VideoTelephonyNotificationRepository videoTelephonyNotificationRepository;

    // 사용자의 알림 목록 가져오기
    public NotificationService(FriendRequestNotificationRepository friendRequestNotificationRepository,
                               VideoTelephonyNotificationRepository videoTelephonyNotificationRepository) {
        this.friendRequestNotificationRepository = friendRequestNotificationRepository;
        this.videoTelephonyNotificationRepository = videoTelephonyNotificationRepository;
    }

    // 사용자의 모든 알림을 가져와 시간 순으로 정렬
    public List<Notification> getAllNotificationsByUserId(Long userId) {
        // 각 테이블에서 알림을 가져옴
        List<FriendRequestNotification> friendRequestNotifications = friendRequestNotificationRepository.findByReceiptUserId(userId);
        List<VideoTelephonyNotification> videoNotifications = videoTelephonyNotificationRepository.findByReceiptUserId(userId);

        // 알림 합치기
        List<Notification> allNotifications = new ArrayList<>();
        allNotifications.addAll(friendRequestNotifications);
        allNotifications.addAll(videoNotifications);

        // createdAt 기준으로 정렬하여 반환
        return allNotifications.stream()
                .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt())) // 내림차순 정렬
                .collect(Collectors.toList());
    }

    // 안 읽은 알림 개수 세기
    public long countUnreadNotifications(Long userId) {
        return friendRequestNotificationRepository.countByReceiptUserIdAndViewFalse(userId)
                + videoTelephonyNotificationRepository.countByReceiptUserIdAndViewFalse(userId);
    }

    public boolean markAsRead(String notificationType, Long notificationId) {
        switch (notificationType.toLowerCase()) {
            case "friend-request":
                Optional<FriendRequestNotification> friendNotification = friendRequestNotificationRepository.findById(notificationId);
                if (friendNotification.isPresent()) {
                    FriendRequestNotification notification = friendNotification.get();
                    notification.setView(true); // 읽음 처리
                    friendRequestNotificationRepository.save(notification); // 업데이트된 상태 저장
                    return true;
                }
                break;
            case "video-telephony":
                Optional<VideoTelephonyNotification> videoNotification = videoTelephonyNotificationRepository.findById(notificationId);
                if (videoNotification.isPresent()) {
                    VideoTelephonyNotification notification = videoNotification.get();
                    notification.setView(true); // 읽음 처리
                    videoTelephonyNotificationRepository.save(notification); // 업데이트된 상태 저장
                    return true;
                }
                break;
            default:
                // 지원되지 않는 타입일 경우
                return false;
        }
        return false;
    }

}
