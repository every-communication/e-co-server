package com.eco.ecoserver.domain.notification.service;

import com.eco.ecoserver.domain.friend.dto.CreateFriendRequestListDTO;
import com.eco.ecoserver.domain.notification.FriendRequestNotification;
import com.eco.ecoserver.domain.notification.Notification;
import com.eco.ecoserver.domain.notification.NotificationType;
import com.eco.ecoserver.domain.notification.VideoTelephonyNotification;
import com.eco.ecoserver.domain.notification.dto.NotificationDto;
import com.eco.ecoserver.domain.notification.dto.VideoNotificationDto;
import com.eco.ecoserver.domain.notification.repository.FriendRequestNotificationRepository;
import com.eco.ecoserver.domain.notification.repository.VideoTelephonyNotificationRepository;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.domain.videotelephony.Room;
import com.eco.ecoserver.domain.videotelephony.dto.RoomDto;
import com.eco.ecoserver.domain.videotelephony.repository.RoomRepository;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.eco.ecoserver.global.sse.service.SseEmitterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
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
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomRepository roomRepository;


    public void createFriendRequestNotification(Long friendRequestListId, CreateFriendRequestListDTO createFriendRequestListDTO) throws IOException {
        Long requestUserId = createFriendRequestListDTO.getUserId();
        Long receiptUserId = createFriendRequestListDTO.getFriendId();

        FriendRequestNotification friendRequestNotification = new FriendRequestNotification(
                friendRequestListId, requestUserId, receiptUserId
        );
        friendRequestNotificationRepository.save(friendRequestNotification);
        sseEmitterService.sendNotification(receiptUserId, "friend-request", "친구 요청을 받았습니다");
    }

    public void createVideoTelephonyNotification(Room room) {
        VideoTelephonyNotification videoTelephonyNotification = new VideoTelephonyNotification(
                room.getId(), room.getOwnerId(), room.getFriendId()
        );
        videoTelephonyNotificationRepository.save(videoTelephonyNotification);

        String requestUserEmail = userRepository.findById(room.getOwnerId())
                .map(User::getEmail)
                .orElse("Unknown");
        VideoNotificationDto videoNotificationDto = new VideoNotificationDto();
        videoNotificationDto.setTitle("화상통화 초대");
        videoNotificationDto.setMessage("화상통화 초대를 받았습니다");

        videoNotificationDto.setRoomCode(room.getCode());
        videoNotificationDto.setNotificationId(videoTelephonyNotification.getId());
        videoNotificationDto.setRequestUserId(room.getOwnerId());
        videoNotificationDto.setRequestUserEmail(requestUserEmail);

        videoNotificationDto.setTimestamp(LocalDateTime.now());
        videoNotificationDto.setNotificationType(NotificationType.VIDEO_TELEPHONY);


        try {
            String notificationJson = new ObjectMapper().writeValueAsString(videoNotificationDto);
            sseEmitterService.sendNotification(room.getFriendId(), "video-telephony", notificationJson);
        } catch (IOException e) {
            log.error("Failed to send notification to user with ID: " + room.getFriendId(), e);
        }
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

    public ApiResponseDto<List<VideoNotificationDto>> getVideoNotifications(Long userId) {
        List<VideoTelephonyNotification> videoNotifications = videoTelephonyNotificationRepository.findByReceiptUserId(userId);

        List<VideoNotificationDto> notificationDtos = videoNotifications.stream()
                .filter(notification -> {
                    // roomRepository에서 Room을 찾아 createdAt이 null인지 확인
                    Room room = roomRepository.findById(notification.getRoomId()).orElse(null);
                    return room != null && room.getCreatedAt() == null;
                })
                .map(notification -> {
                    Room room = roomRepository.findById(notification.getRoomId()).orElse(null);
                    String roomCode = (room != null) ? room.getCode() : "Unknown";

                    VideoNotificationDto dto = new VideoNotificationDto();
                    dto.setTitle(notification.getTitle());
                    dto.setMessage(notification.getMessage());
                    dto.setNotificationId(notification.getId());
                    dto.setRoomCode(roomCode); // Room의 roomCode를 설정
                    dto.setRequestUserId(notification.getRequestUserId());
                    dto.setRequestUserEmail(userService.findEmailById(notification.getRequestUserId())); // requestUserId로 이메일 조회
                    dto.setTimestamp(notification.getCreatedAt());
                    dto.setNotificationType(notification.getNotificationType());
                    return dto;
                })
                .collect(Collectors.toList());

        // 성공 응답으로 ApiResponseDto 반환
        return ApiResponseDto.success(notificationDtos);
    }
}
