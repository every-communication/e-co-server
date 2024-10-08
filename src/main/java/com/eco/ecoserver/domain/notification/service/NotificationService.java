package com.eco.ecoserver.domain.notification.service;

import com.eco.ecoserver.domain.friend.dto.CreateFriendListDTO;
import com.eco.ecoserver.domain.friend.dto.CreateFriendRequestListDTO;
import com.eco.ecoserver.domain.notification.FriendRequestNotification;
import com.eco.ecoserver.domain.notification.Notification;
import com.eco.ecoserver.domain.notification.NotificationType;
import com.eco.ecoserver.domain.notification.VideoTelephonyNotification;
import com.eco.ecoserver.domain.notification.dto.NotificationDto;
import com.eco.ecoserver.domain.notification.repository.FriendRequestNotificationRepository;
import com.eco.ecoserver.domain.notification.repository.VideoTelephonyNotificationRepository;
import com.eco.ecoserver.domain.user.Role;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.eco.ecoserver.global.sse.service.SseEmitterService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
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
    @Autowired
    private SseEmitterService sseEmitterService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;

    public void createNotification(Long friendRequestListId, CreateFriendRequestListDTO createFriendRequestListDTO) throws IOException {
        Long requestUserId = createFriendRequestListDTO.getUserId();
        Long receiptUserId = createFriendRequestListDTO.getFriendId();

        FriendRequestNotification friendRequestNotification = new FriendRequestNotification(
                friendRequestListId, requestUserId, receiptUserId
        );
        friendRequestNotificationRepository.save(friendRequestNotification);
        sseEmitterService.sendNotification(receiptUserId, "friend-request", "친구 요청을 받았습니다");
    }

    public void createNotification() {} //화상통화용

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
    public long countUnreadNotifications(Long userId) throws IOException {
        long unreadCount = friendRequestNotificationRepository.countByReceiptUserIdAndViewFalse(userId)
                + videoTelephonyNotificationRepository.countByReceiptUserIdAndViewFalse(userId);
        sseEmitterService.sendNotification(userId, "unread-count", String.valueOf(unreadCount));
        return unreadCount;
    }

    public boolean markAsRead(HttpServletRequest request, String notificationType, Long notificationId) throws IOException {
        Optional<String> emailOpt = jwtService.extractEmailFromToken(request);
        if(emailOpt.isEmpty()) {
            return false;
        }
        Optional<User> userOpt = userService.findByEmail(emailOpt.get());
        if(userOpt.isEmpty()) {
            return false;
        }
        switch (notificationType.toLowerCase()) {
            case "friend-request":
                Optional<FriendRequestNotification> friendNotification = friendRequestNotificationRepository.findById(notificationId);
                if (friendNotification.isPresent()) {
                    FriendRequestNotification notification = friendNotification.get();
                    notification.setView(true); // 읽음 처리
                    friendRequestNotificationRepository.save(notification); // 업데이트된 상태 저장
                    countUnreadNotifications(userOpt.get().getId());
                    return true;
                }
                break;
            case "video-telephony":
                Optional<VideoTelephonyNotification> videoNotification = videoTelephonyNotificationRepository.findById(notificationId);
                if (videoNotification.isPresent()) {
                    VideoTelephonyNotification notification = videoNotification.get();
                    notification.setView(true); // 읽음 처리
                    videoTelephonyNotificationRepository.save(notification); // 업데이트된 상태 저장
                    countUnreadNotifications(userOpt.get().getId());
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
