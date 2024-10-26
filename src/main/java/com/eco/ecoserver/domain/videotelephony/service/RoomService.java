package com.eco.ecoserver.domain.videotelephony.service;


import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.videotelephony.Room;
import com.eco.ecoserver.domain.videotelephony.repository.RoomRepository;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {


    private final RoomRepository roomRepository;



    public Room createRoom(User user) {
        Room room = new Room();
        room.updateUser1(user.getId());
        room.setCode(UUID.randomUUID().toString());
        return roomRepository.save(room);
    }

    public Room createRoomWithFriend(User user, Long friendId) {
        Room room = new Room();
        room.updateUser1(user.getId());
        room.updateUser2(friendId);
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
        Room room = roomRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Room not found"));
        if (room.getUser1Id() == null|| !room.getUser1Id().equals(userId)) {
            room.updateUser1(userId);
        } else if (room.getUser2Id() == null|| !room.getUser2Id().equals(userId)) {
            room.updateUser2(userId);
        } else {
            throw new RuntimeException("Room is full");
        }
        return roomRepository.save(room);
    }

    public Room leaveRoom(String code, Long userId) {
        Room room = roomRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Room not found"));
        if (room.getUser1Id().equals(userId)) {
            room.updateUser1(null);
//            room.setMic1(false);
//            room.setCam1(false);
        } else if (room.getUser2Id().equals(userId)) {
            room.updateUser2(null);
//            room.setMic2(false);
//            room.setCam2(false);
        }
        if(room.getUser1Id()==null&&room.getUser2Id()==null){
            room.setDeletedAt(LocalDateTime.now());
        }
        return roomRepository.save(room);
    }

    public Room updateMediaStatus(String code, Long userId, boolean mic, boolean cam) {
        Room room = roomRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Room not found"));
        if (room.getUser1Id().equals(userId)) {
            room.updateMic1(mic);
            room.updateCam1(cam);
        } else if (room.getUser2Id().equals(userId)) {
            room.updateMic2(mic);
            room.updateCam2(cam);
        } else {
            throw new RuntimeException("User not in room");
        }
        return roomRepository.save(room);
    }


    public ResponseEntity<?> getRoom(String code, User user) {
        return roomRepository.findByCode(code)
                .map(room -> {
                    if (room.getDeletedAt() != null) {
                        return ResponseEntity.status(400)
                                .body(ApiResponseDto.failure(400, "Room is deleted"));
                    }

                    // user1Id와 user2Id가 모두 존재하고, 현재 유저가 둘 다 아닌 경우
                    if (room.getUser1Id() != null && room.getUser2Id() != null &&
                            !room.getUser1Id().equals(user.getId()) && !room.getUser2Id().equals(user.getId())) {
                        return ResponseEntity.status(400)
                                .body(ApiResponseDto.failure(400, "You are not a participant of this room"));
                    }

                    return ResponseEntity.ok(ApiResponseDto.success(room));
                })
                .orElseGet(() -> ResponseEntity.status(400)
                        .body(ApiResponseDto.failure(400, "Room not found")));
    }
}

