package com.eco.ecoserver.domain.videotelephony.service;


import com.eco.ecoserver.domain.friend.FriendList;
import com.eco.ecoserver.domain.friend.repository.FriendListRepository;
import com.eco.ecoserver.domain.notification.service.NotificationService;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.domain.videotelephony.Room;
import com.eco.ecoserver.domain.videotelephony.dto.CallInfoDto;
import com.eco.ecoserver.domain.videotelephony.repository.RoomRepository;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.sse.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final FriendListRepository friendListRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public Room createRoom(Long userId) {
        Room room = new Room();
        room.setOwnerId(userId);
        room.setCode(UUID.randomUUID().toString());

        return roomRepository.save(room);
    }

    public Room createRoomWithFriend(Long userId, Long friendId) {
        Room room = new Room();
        room.setOwnerId(userId);
        room.setFriendId(friendId);
        room.setCode(UUID.randomUUID().toString());

        return roomRepository.save(room);
    }

    public Optional<Room> findRoomByCode(String code) {
        return roomRepository.findByCode(code);
    }

    public Room updateRoom(Room room) {
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room joinRoom(String code, Long userId) {
        Room room = roomRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        if (room.getOwnerId().equals(userId)) {
            room.updateUser1(userId);

        } else if (room.getUser2Id() == null && !room.getUser1Id().equals(userId)) {
            room.updateUser2(userId);
            room.setFriendId(userId);
            room.setCreatedAt(LocalDateTime.now());
        } else if(room.getFriendId()!=null && !room.getFriendId().equals(userId)){
            throw new RuntimeException("Room is full");
        } else{
            throw new RuntimeException("Room is full");
        }
        return roomRepository.save(room);
    }

    public Room leaveRoom(String code, Long userId) {
        Room room = roomRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 방입니다."));
        if (room.getUser1Id() != null && room.getUser1Id().equals(userId)) {
            room.updateUser1(null);
        } else if (room.getUser2Id() != null && room.getUser2Id().equals(userId)) {
            room.updateUser2(null);
        }
        if(room.getUser1Id() == null && room.getUser2Id() == null && room.getCreatedAt()!=null) {
            room.setDeletedAt(LocalDateTime.now());
        }
        return roomRepository.save(room);
    }

    public Room updateMediaStatus(String code, Long userId, boolean mic, boolean cam) {
        Room room = roomRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        if (room.getUser1Id() != null && room.getUser1Id().equals(userId)) {
            room.updateMic1(mic);
            room.updateCam1(cam);
        } else if (room.getUser2Id() != null && room.getUser2Id().equals(userId)) {
            room.updateMic2(mic);
            room.updateCam2(cam);
        } else {
            throw new RuntimeException("User not in room");
        }
        return roomRepository.save(room);
    }

    public ResponseEntity<?> getRoom(String code, Long userId) {
        return roomRepository.findByCode(code)
                .map(room -> {
                    if (room.getDeletedAt() != null) {
                        return ResponseEntity.status(400)
                                .body(ApiResponseDto.failure(400, "통화가 종료된 방입니다."));
                    }

                    // user1Id와 user2Id가 모두 존재하고, 현재 유저가 둘 다 아닌 경우
                    if (room.getUser1Id() != null && room.getUser2Id() != null &&
                            !room.getUser1Id().equals(userId) && !room.getUser2Id().equals(userId)) {
                        return ResponseEntity.status(400)
                                .body(ApiResponseDto.failure(400, "접속할 수 있는 방이 아닙니다."));
                    }

                    return ResponseEntity.ok(ApiResponseDto.success(room));
                })
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(ApiResponseDto.failure(404, "존재하지 않는 방입니다.")));
    }

    public Long getFriendId(String code, Long userId) {
        Optional<Room> room  = roomRepository.findByCode(code);
        if(room.isPresent()){
            Room room1 = room.get();
            if(room1.getUser1Id()!=null){
                if(!room1.getUser1Id().equals(userId)){
                    return room1.getUser1Id();
                }
            }
            if(room1.getUser2Id()!=null){
                if(!room1.getUser2Id().equals(userId)){
                    return room1.getUser2Id();
                }
            }

        }
        return null;
    }

    public List<CallInfoDto> getRecentCalls(User user) {
        List<Room> rooms = roomRepository.findByOwnerId(user.getId());
        rooms.addAll(roomRepository.findByFriendId(user.getId()));
        List<CallInfoDto> callInfoDtos = new ArrayList<>();

        for(Room room : rooms){
            if(room.getDeletedAt()!=null) {
                User friend = getFriendUser(user, room);

                if (friend!=null){
                    String friendName = friend.getNickname();
                    String friendEmail = friend.getEmail();
                    String friendThumbnail = friend.getThumbnail();

                    // 친구 여부 확인
                    boolean isFriend = !friendListRepository.findByUserIdAndFriendId(user.getId(), friend.getId()).isEmpty();

                    // duration을 초 단위로 계산
                    long durationInSeconds = ChronoUnit.SECONDS.between(room.getCreatedAt(), room.getDeletedAt());

                    // deletedAt을 문자열 형식으로 설정
                    ZonedDateTime deletedAtKST = room.getDeletedAt().atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("Asia/Seoul"));
                    String deletedAtStr = deletedAtKST.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    // CallInfoDto 생성 및 리스트에 추가
                    callInfoDtos.add(new CallInfoDto(
                            friend.getId(),
                            friendName,
                            friendEmail,
                            friendThumbnail,
                            isFriend,
                            durationInSeconds,
                            deletedAtStr
                    ));
                }
            }
        }
        // deletedAt 기준으로 내림차순 정렬
        callInfoDtos.sort((dto1, dto2) -> dto2.getDeletedAt().compareTo(dto1.getDeletedAt()));

        return callInfoDtos;
    }

    // 친구 정보를 가져오는 헬퍼 메서드
    private User getFriendUser(User user, Room room) {
        Long friendId = room.getOwnerId().equals(user.getId()) ? room.getFriendId() : room.getOwnerId();
        return userRepository.findById(friendId).orElse(null);
    }
}

