package com.eco.ecoserver.domain.videotelephony.controller;

import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.domain.videotelephony.Room;
import com.eco.ecoserver.domain.videotelephony.service.RoomService;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.jwt.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private final JwtService jwtService;
    private final UserService userService;

    @GetMapping("/{code}")
    public ResponseEntity<?> getRoom(HttpServletRequest request, @PathVariable("code")String code){
        Optional<String> email = jwtService.extractEmailFromToken(request);
        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorized"));
        }

        Optional<User> user = userService.findByEmail(email.get());
        return user.map(value -> {
            ResponseEntity<?> room = roomService.getRoom(code, value);
            if(room.getStatusCode().equals(HttpStatus.OK)){
                return ResponseEntity.ok(ApiResponseDto.success(room.getBody()));
            }
            else{
                return room;
            }

        }).orElseGet(() -> ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorized")));

    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<Room>> createRoom(HttpServletRequest request) {
        Optional<String> email = jwtService.extractEmailFromToken(request);
        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorized"));
        }

        Optional<User> user = userService.findByEmail(email.get());
        return user.map(value -> {
            Room room = roomService.createRoom(value);
            return ResponseEntity.ok(ApiResponseDto.success(room));
        }).orElseGet(() -> ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorized")));
    }

    @PostMapping("/{friendId}")
    public ResponseEntity<ApiResponseDto<Room>> createRoomWithFriend(HttpServletRequest request, @PathVariable Long friendId) {
        Optional<String> email = jwtService.extractEmailFromToken(request);
        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorized"));
        }

        Optional<User> user = userService.findByEmail(email.get());
        return user.map(value -> {
            Room room = roomService.createRoomWithFriend(value, friendId);
            return ResponseEntity.ok(ApiResponseDto.success(room));
        }).orElseGet(() -> ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorized")));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<Room>>> getAllRooms(HttpServletRequest request) {
        Optional<String> email = jwtService.extractEmailFromToken(request);
        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorized"));
        }

        List<Room> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(ApiResponseDto.success(rooms));
    }

    @PostMapping("/join/{code}")
    public ResponseEntity<?> joinRoom(@PathVariable String code, HttpServletRequest request) {
        Optional<String> email = jwtService.extractEmailFromToken(request);
        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorized"));
        }

        Optional<User> user = userService.findByEmail(email.get());
        return user.map(value -> {
            try {
                Room room = roomService.joinRoom(code, value.getId());
                return ResponseEntity.ok(ApiResponseDto.success(room));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(ApiResponseDto.failure(400, e.getMessage()));
            }
        }).orElseGet(() -> ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorized")));
    }

    @PostMapping("/leave/{code}")
    public ResponseEntity<ApiResponseDto<Room>> leaveRoom(@PathVariable String code, HttpServletRequest request) {
        Optional<String> email = jwtService.extractEmailFromToken(request);
        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorized"));
        }

        Optional<User> user = userService.findByEmail(email.get());
        return user.map(value -> {
            Room room = roomService.leaveRoom(code, value);
            return ResponseEntity.ok(ApiResponseDto.success(room));
        }).orElseGet(() -> ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorized")));
    }

    @PutMapping("/media/{code}")
    public ResponseEntity<ApiResponseDto<Room>> updateMediaStatus(
            @PathVariable String code,
            @RequestParam boolean mic,
            @RequestParam boolean cam,
            HttpServletRequest request) {
        Optional<String> email = jwtService.extractEmailFromToken(request);
        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorized"));
        }

        Optional<User> user = userService.findByEmail(email.get());
        return user.map(value -> {
            Room roomDTO = roomService.updateMediaStatus(code, value.getId(), mic, cam);
            return ResponseEntity.ok(ApiResponseDto.success(roomDTO));
        }).orElseGet(() -> ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorized")));
    }
}