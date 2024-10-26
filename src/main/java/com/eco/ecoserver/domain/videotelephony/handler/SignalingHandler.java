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
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SignalingHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final RoomService roomService;  // roomService만 필요

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
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
        }
    }

    private void handleCreateRoom(WebSocketSession session, Long userId) throws IOException {
        try {
            Room room = roomService.createRoom(userId);  // User 객체 대신 userId만 전달
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
            Room room = roomService.joinRoom(roomCode, userId);  // User 객체 대신 userId만 전달
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
        Room room = roomService.leaveRoom(roomCode, userId);  // User 객체 대신 userId만 전달
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
        // 필요한 초기화 작업
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
    }
}