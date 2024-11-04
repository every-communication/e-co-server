package com.eco.ecoserver.domain.videotelephony.handler;

import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.domain.videotelephony.Room;
import com.eco.ecoserver.domain.videotelephony.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SignalingHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(SignalingHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final RoomService roomService;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) payload.get("type");

            Object userIdObj = payload.get("userId");
            Long userId = null;
            if (userIdObj != null) {
                if (userIdObj instanceof Integer) {
                    userId = ((Integer) userIdObj).longValue();
                } else if (userIdObj instanceof Long) {
                    userId = (Long) userIdObj;
                }
            }
            String msg = (String) payload.get("message");
            boolean mic = (boolean) payload.get("mic");
            boolean cam = (boolean) payload.get("cam");


            switch (type) {
                case "createRoom":
                    handleCreateRoom(session, userId);
                    break;
                case "joinRoom":
                    handleJoinRoom(session, (String) payload.get("room"), userId);
                    break;
                case "leaveRoom":
                    handleLeaveRoom(session, (String) payload.get("room"), userId);
                    break;
                case "offer":
                case "answer":
                case "candidate":
                    handleSignaling(session, payload);
                    break;
                case "getRooms":
                    sendRoomList(session, userId);
                    break;
                case "translation":
                    translateSignLangauge(session,(String) payload.get("room"), userId,msg);
                    break;
                case "changeMedia":
                    changeMedia(session,(String) payload.get("room"), userId, cam, mic);
                    break;
                default:
                    logger.error("Unknown message type: {}", type);
                    break;
            }
        } catch (Exception e) {
            logger.error("Error handling WebSocket message", e);
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                        "type", "error",
                        "message", e.getMessage()
                ))));
            } catch (IOException ex) {
                logger.error("Error sending error message to client", ex);
            }
        }
    }

    private void changeMedia(WebSocketSession session, String code, Long userId, boolean cam, boolean mic) throws IOException{
        try {

            roomService.updateMediaStatus(code, userId, mic, cam);
            Optional<Room> room = roomService.findRoomByCode(code);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "changedMedia",
                    "room", room.get()
            ))));
            sendRoomList(null, userId);
        } catch (RuntimeException e) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "error",
                    "message", e.getMessage()
            ))));
        }
    }

    private void translateSignLangauge(WebSocketSession session, String room, Long userId, String msg) throws IOException {
        Long friendId = roomService.getFriendId(room, userId);
        if(friendId!=null){
            for (WebSocketSession s : sessions.values()) {
                s.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                        "type", String.format("translated-%d-%s", friendId, room),
                        "message", msg
                ))));
            }
        }
    }

    private void handleCreateRoom(WebSocketSession session, Long userId) throws IOException {
        try {
            Room room = roomService.createRoom(userId);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "roomCreated",
                    "room", room
            ))));
            sendRoomList(null, userId);
        } catch (RuntimeException e) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "error",
                    "message", e.getMessage()
            ))));
        }
    }

    private void handleJoinRoom(WebSocketSession session, String roomCode, Long userId) throws IOException {
        try {
            Room room = roomService.joinRoom(roomCode, userId);
            sessions.put(session.getId(), session);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "joinedRoom",
                    "room", room
            ))));
            sendRoomList(null, userId);
        } catch (RuntimeException e) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "error",
                    "message", e.getMessage()
            ))));
        }
    }

    private void handleLeaveRoom(WebSocketSession session, String roomCode, Long userId) throws IOException {
        Room room = roomService.leaveRoom(roomCode, userId);
        sessions.remove(session.getId());
        sendRoomList(null, userId);

        for (WebSocketSession s : sessions.values()) {
            s.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "participantLeft",
                    "room", room,
                    "userId", userId
            ))));
        }
    }

    private void handleSignaling(WebSocketSession session, Map<String, Object> payload) throws IOException {
        String roomCode = (String) payload.get("room");
        Room room = roomService.findRoomByCode(roomCode).orElseThrow(() -> new RuntimeException("Room not found"));

        for (WebSocketSession s : sessions.values()) {
            if (!s.getId().equals(session.getId())) {
                s.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
            }
        }
    }

    private void sendRoomList(WebSocketSession session, Long userId) throws IOException {
        List<Room> rooms = roomService.getAllRooms();
        String messageJson = objectMapper.writeValueAsString(Map.of(
                "type", "roomList",
                "rooms", rooms
        ));

        if (session != null) {
            session.sendMessage(new TextMessage(messageJson));
        } else {
            for (WebSocketSession s : sessions.values()) {
                s.sendMessage(new TextMessage(messageJson));
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed: {}, status: {}", session.getId(), status);
        sessions.remove(session.getId());
    }
}